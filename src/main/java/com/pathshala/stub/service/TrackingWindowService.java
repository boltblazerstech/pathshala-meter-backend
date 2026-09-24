package com.pathshala.stub.service;

import com.pathshala.stub.dto.*;
import com.pathshala.stub.entity.TrackingWindow;
import com.pathshala.stub.entity.User;
import com.pathshala.stub.repository.TrackingWindowRepository;
import com.pathshala.stub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TrackingWindowService {

    private static final ZoneId          IST       = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter HH_MM    = DateTimeFormatter.ofPattern("HH:mm");

    private final TrackingWindowRepository windowRepository;
    private final UserRepository           userRepository;
    private final com.pathshala.stub.repository.PaathshalaRepository paathshalaRepository;
    private final SupervisorTimetableService timetableService;
    private final com.pathshala.stub.repository.SystemConfigRepository systemConfigRepository;

    public TrackingWindowService(TrackingWindowRepository windowRepository,
                                 UserRepository userRepository,
                                 com.pathshala.stub.repository.PaathshalaRepository paathshalaRepository,
                                 SupervisorTimetableService timetableService,
                                 com.pathshala.stub.repository.SystemConfigRepository systemConfigRepository) {
        this.windowRepository = windowRepository;
        this.userRepository   = userRepository;
        this.paathshalaRepository = paathshalaRepository;
        this.timetableService = timetableService;
        this.systemConfigRepository = systemConfigRepository;
    }

    // ── Admin: Create ─────────────────────────────────────────────────

    @Transactional
    public AdminTrackingWindowResponse create(CreateTrackingWindowRequest req) {
        validateUser(req.userId());
        LocalTime start = parseTime(req.startTime(), "start_time");
        LocalTime end   = parseTime(req.endTime(),   "end_time");
        validateWindow(start, end, req.intervalMinutes(), req.effectiveFromDate());

        TrackingWindow window = buildWindow(req.userId(), start, end,
                req.intervalMinutes(), req.effectiveFromDate());
        return toAdminResponse(windowRepository.save(window));
    }

    // ── Admin: Bulk create ────────────────────────────────────────────

    @Transactional
    public List<AdminTrackingWindowResponse> createBulk(BulkTrackingWindowRequest req) {
        LocalTime start = parseTime(req.startTime(), "start_time");
        LocalTime end   = parseTime(req.endTime(),   "end_time");
        validateWindow(start, end, req.intervalMinutes(), req.effectiveFromDate());

        // Validate all user IDs exist before inserting any
        for (UUID uid : req.userIds()) {
            validateUser(uid);
        }

        List<TrackingWindow> windows = req.userIds().stream()
                .map(uid -> buildWindow(uid, start, end,
                        req.intervalMinutes(), req.effectiveFromDate()))
                .collect(Collectors.toList());

        return windowRepository.saveAll(windows).stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    // ── Admin: GET all effective as of a date ─────────────────────────

    @Transactional(readOnly = true)
    public List<EffectiveWindowEntry> findAllEffectiveAsOf(LocalDate date) {
        List<TrackingWindow> windows = windowRepository.findAllEffectiveAsOf(date);

        // Batch-fetch users to avoid N+1
        Set<UUID> userIds = windows.stream()
                .map(TrackingWindow::getUserId)
                .collect(Collectors.toSet());
        Map<UUID, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return windows.stream()
                .map(w -> {
                    User user = userMap.get(w.getUserId());
                    return new EffectiveWindowEntry(
                            w.getId(),
                            w.getUserId(),
                            user != null ? user.getName() : null,
                            user != null ? user.getRole() : null,
                            w.getStartTime().format(HH_MM),
                            w.getEndTime().format(HH_MM),
                            w.getIntervalMinutes(),
                            w.getEffectiveFromDate()
                    );
                })
                .collect(Collectors.toList());
    }

    // ── Admin: Update ─────────────────────────────────────────────────

    @Transactional
    public AdminTrackingWindowResponse update(UUID id, UpdateTrackingWindowRequest req) {
        TrackingWindow window = windowRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tracking window not found: " + id));

        LocalTime start = (req.startTime() != null)
                ? parseTime(req.startTime(), "start_time")
                : window.getStartTime();
        LocalTime end   = (req.endTime() != null)
                ? parseTime(req.endTime(), "end_time")
                : window.getEndTime();
        int interval = (req.intervalMinutes() != null)
                ? req.intervalMinutes()
                : window.getIntervalMinutes();
        LocalDate date = (req.effectiveFromDate() != null)
                ? req.effectiveFromDate()
                : window.getEffectiveFromDate();

        // Re-validate the combined values after any update
        validateWindow(start, end, interval, date);

        window.setStartTime(start);
        window.setEndTime(end);
        window.setIntervalMinutes(interval);
        window.setEffectiveFromDate(date);

        return toAdminResponse(windowRepository.save(window));
    }

    // ── Field-app: current window for the calling user ────────────────

    @Transactional(readOnly = true)
    public List<TrackingWindowDto> findWindowsForUser(UUID userId) {
        LocalDate today = LocalDate.now(IST);

        // a) FIRST check for a manual override in the existing tracking_windows table
        Optional<TrackingWindow> manualOverride = windowRepository.findCurrentForUser(userId, today);
        if (manualOverride.isPresent()) {
            TrackingWindow w = manualOverride.get();
            return List.of(new TrackingWindowDto(
                    w.getStartTime().format(HH_MM),
                    w.getEndTime().format(HH_MM),
                    w.getIntervalMinutes(),
                    null // Manual overrides don't carry a specific paathshaala_id in this context
            ));
        }

        // b) If no manual override, derive from role and paathshaala
        User user = userRepository.findById(userId).orElseThrow();
        
        int preBuffer = getConfigInt("pre_buffer_minutes", 45);
        int postBuffer = getConfigInt("post_buffer_minutes", 45);
        int fetchInterval = getConfigInt("fetch_interval_minutes", 10);

        if ("teacher".equals(user.getRole())) {
            if (user.getAssignedPaathshalaId() == null) {
                return Collections.emptyList();
            }
            return paathshalaRepository.findById(user.getAssignedPaathshalaId())
                    .map(p -> computeWindow(p, preBuffer, postBuffer, fetchInterval))
                    .map(w -> w != null ? List.of(w) : Collections.<TrackingWindowDto>emptyList())
                    .orElse(Collections.emptyList());
        }

        if ("supervisor".equals(user.getRole())) {
            int dow = today.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
            
            // Fetch overrides for today
            List<TimetableOverrideDto> overrides = timetableService.getOverrides(userId, today, today);
            Map<String, UUID> overrideMap = overrides.stream()
                    .collect(Collectors.toMap(TimetableOverrideDto::slot, TimetableOverrideDto::paathshaalaId));
            
            // Fetch weekly timetable
            TimetableResponse timetable = timetableService.getTimetable(userId);
            Map<String, UUID> weeklyMap = timetable.slots().stream()
                    .filter(s -> s.dayOfWeek() == dow)
                    .collect(Collectors.toMap(TimetableSlotDto::slot, TimetableSlotDto::paathshaalaId));

            List<TrackingWindowDto> windows = new ArrayList<>();
            
            for (String slot : List.of("MORNING", "EVENING")) {
                UUID pid;
                if (overrideMap.containsKey(slot)) {
                    pid = overrideMap.get(slot); // even if null (explicitly skipped)
                } else {
                    pid = weeklyMap.get(slot);
                }
                
                if (pid != null) {
                    paathshalaRepository.findById(pid)
                            .map(p -> computeWindow(p, preBuffer, postBuffer, fetchInterval))
                            .ifPresent(windows::add);
                }
            }
            return windows;
        }

        return Collections.emptyList();
    }
    
    public com.pathshala.stub.entity.Paathshaala resolveTargetPaathshaalaForTime(User user, Instant pointTime) {
        if ("teacher".equals(user.getRole())) {
            if (user.getAssignedPaathshalaId() == null) return null;
            return paathshalaRepository.findById(user.getAssignedPaathshalaId()).orElse(null);
        }

        if ("supervisor".equals(user.getRole())) {
            LocalDate today = pointTime.atZone(IST).toLocalDate();
            LocalTime time = pointTime.atZone(IST).toLocalTime();
            int dow = today.getDayOfWeek().getValue();
            
            // Cutoff for MORNING vs EVENING is 14:00 (2:00 PM)
            String slot = time.isBefore(LocalTime.of(14, 0)) ? "MORNING" : "EVENING";

            List<TimetableOverrideDto> overrides = timetableService.getOverrides(user.getId(), today, today);
            for (TimetableOverrideDto o : overrides) {
                if (slot.equals(o.slot())) {
                    if (o.paathshaalaId() == null) return null;
                    return paathshalaRepository.findById(o.paathshaalaId()).orElse(null);
                }
            }

            TimetableResponse timetable = timetableService.getTimetable(user.getId());
            for (TimetableSlotDto s : timetable.slots()) {
                if (s.dayOfWeek() == dow && slot.equals(s.slot())) {
                    if (s.paathshaalaId() == null) return null;
                    return paathshalaRepository.findById(s.paathshaalaId()).orElse(null);
                }
            }
        }
        return null;
    }
    
    private TrackingWindowDto computeWindow(com.pathshala.stub.entity.Paathshaala p, int preBuffer, int postBuffer, int fetchInterval) {
        if (p.getOpeningTime() == null || p.getClosingTime() == null) {
            return null;
        }
        LocalTime start = p.getOpeningTime().minusMinutes(preBuffer);
        LocalTime end = p.getClosingTime().plusMinutes(postBuffer);
        return new TrackingWindowDto(start.format(HH_MM), end.format(HH_MM), fetchInterval, p.getId());
    }

    public int getConfigInt(String key, int defaultValue) {
        return systemConfigRepository.findById(key)
                .map(sc -> Integer.parseInt(sc.getValue()))
                .orElse(defaultValue);
    }

    // ── Validation helpers ────────────────────────────────────────────

    private void validateUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }

    private void validateWindow(LocalTime start, LocalTime end,
                                int intervalMinutes, LocalDate effectiveFromDate) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "start_time (" + start.format(HH_MM) + ") must be before "
                            + "end_time (" + end.format(HH_MM) + ")");
        }
        if (intervalMinutes < 1) {
            throw new IllegalArgumentException("interval_minutes must be at least 1");
        }
        LocalDate today = LocalDate.now(IST);
        if (effectiveFromDate.isBefore(today)) {
            throw new IllegalArgumentException(
                    "effective_from_date " + effectiveFromDate
                            + " is in the past (today is " + today + " IST)");
        }
    }

    private LocalTime parseTime(String raw, String fieldName) {
        try {
            return LocalTime.parse(raw.trim(), HH_MM);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    fieldName + " must be in HH:mm format, got: '" + raw + "'");
        }
    }

    private TrackingWindow buildWindow(UUID userId, LocalTime start, LocalTime end,
                                       int interval, LocalDate date) {
        TrackingWindow w = new TrackingWindow();
        w.setUserId(userId);
        w.setStartTime(start);
        w.setEndTime(end);
        w.setIntervalMinutes(interval);
        w.setEffectiveFromDate(date);
        w.setActive(true);
        return w;
    }

    private AdminTrackingWindowResponse toAdminResponse(TrackingWindow w) {
        return new AdminTrackingWindowResponse(
                w.getId(),
                w.getUserId(),
                w.getStartTime().format(HH_MM),
                w.getEndTime().format(HH_MM),
                w.getIntervalMinutes(),
                w.getEffectiveFromDate(),
                w.isActive(),
                w.getCreatedAt()
        );
    }
}
