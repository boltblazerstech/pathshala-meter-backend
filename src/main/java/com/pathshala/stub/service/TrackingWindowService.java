package com.pathshala.stub.service;

import com.pathshala.stub.dto.*;
import com.pathshala.stub.entity.TrackingWindow;
import com.pathshala.stub.entity.User;
import com.pathshala.stub.repository.TrackingWindowRepository;
import com.pathshala.stub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public TrackingWindowService(TrackingWindowRepository windowRepository,
                                 UserRepository userRepository) {
        this.windowRepository = windowRepository;
        this.userRepository   = userRepository;
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
    public Optional<TrackingWindowResponse> findCurrentForUser(UUID userId) {
        LocalDate today = LocalDate.now(IST);
        return windowRepository.findCurrentForUser(userId, today)
                .map(w -> new TrackingWindowResponse(
                        w.getStartTime().format(HH_MM),
                        w.getEndTime().format(HH_MM),
                        w.getIntervalMinutes(),
                        0 // overridden in controller
                ));
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
