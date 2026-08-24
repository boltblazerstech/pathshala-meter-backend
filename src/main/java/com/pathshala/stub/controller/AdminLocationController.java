package com.pathshala.stub.controller;

import com.pathshala.stub.dto.AdminLiveLocationResponse;
import com.pathshala.stub.entity.LocationPoint;
import com.pathshala.stub.entity.Paathshaala;
import com.pathshala.stub.entity.User;
import com.pathshala.stub.repository.LocationPointRepository;
import com.pathshala.stub.repository.PaathshalaRepository;
import com.pathshala.stub.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminLocationController {

    private final LocationPointRepository locationRepository;
    private final UserRepository userRepository;
    private final PaathshalaRepository paathshalaRepository;

    public AdminLocationController(LocationPointRepository locationRepository,
                                   UserRepository userRepository,
                                   PaathshalaRepository paathshalaRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.paathshalaRepository = paathshalaRepository;
    }

    /**
     * GET /api/admin/locations/live
     * Returns the single most recent location ping for every user.
     * Enriched with user name, role, and assigned Paathshaala (if any).
     */
    @GetMapping("/locations/live")
    public List<AdminLiveLocationResponse> getLiveLocations() {
        // 1. Fetch the latest ping per user (DISTINCT ON user_id)
        List<LocationPoint> latestPings = locationRepository.findLatestPingsPerUser();
        if (latestPings.isEmpty()) {
            return List.of();
        }

        // 2. Batch fetch the associated users
        Set<java.util.UUID> userIds = latestPings.stream()
                .map(LocationPoint::getUserId)
                .collect(Collectors.toSet());
        Map<java.util.UUID, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 3. Batch fetch the associated Paathshaalas (for teachers)
        Set<java.util.UUID> paathshaalaIds = userMap.values().stream()
                .map(User::getAssignedPaathshalaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<java.util.UUID, Paathshaala> paathshaalaMap = paathshalaRepository.findAllById(paathshaalaIds)
                .stream()
                .collect(Collectors.toMap(Paathshaala::getId, Function.identity()));

        // 4. Map to enriched DTOs
        return latestPings.stream().map(ping -> {
            User user = userMap.get(ping.getUserId());
            String userName = "Unknown";
            String userRole = "Unknown";
            String pName = null;

            if (user != null) {
                userName = user.getName();
                userRole = user.getRole();
                if (user.getAssignedPaathshalaId() != null) {
                    Paathshaala p = paathshaalaMap.get(user.getAssignedPaathshalaId());
                    if (p != null) {
                        pName = p.getName();
                    }
                }
            }

            return new AdminLiveLocationResponse(
                    ping.getUserId(),
                    userName,
                    userRole,
                    pName,
                    ping.getLat(),
                    ping.getLng(),
                    ping.getCapturedAt(),
                    ping.getReceivedAt(),
                    ping.getSyncStatus()
            );
        }).collect(Collectors.toList());
    }

    /**
     * GET /api/admin/locations
     * Admin view of location data. Allows filtering by optional userId and date range.
     * Omitting userId returns locations for all users.
     */
    @GetMapping("/locations")
    public List<AdminLiveLocationResponse> getLocations(
            @org.springframework.web.bind.annotation.RequestParam(value = "user_id", required = false) java.util.UUID userId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to) {
        
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must not be after 'to' date");
        }

        java.time.ZoneId istZone = java.time.ZoneId.of("Asia/Kolkata");
        java.time.Instant fromInstant = (from != null)
                ? from.atStartOfDay(istZone).toInstant()
                : null;
        java.time.Instant toInstant = (to != null)
                ? to.atTime(java.time.LocalTime.MAX).atZone(istZone).toInstant()
                : null;

        List<LocationPoint> history = locationRepository.findByUserIdAndRange(userId, fromInstant, toInstant);
        if (history.isEmpty()) return List.of();

        // Batch fetch users to enrich
        Set<java.util.UUID> userIds = history.stream().map(LocationPoint::getUserId).collect(Collectors.toSet());
        Map<java.util.UUID, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Batch fetch Paathshaalas
        Set<java.util.UUID> paathshaalaIds = userMap.values().stream()
                .map(User::getAssignedPaathshalaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<java.util.UUID, Paathshaala> paathshaalaMap = paathshalaRepository.findAllById(paathshaalaIds)
                .stream()
                .collect(Collectors.toMap(Paathshaala::getId, Function.identity()));

        return history.stream().map(ping -> {
            User user = userMap.get(ping.getUserId());
            String userName = "Unknown";
            String userRole = "Unknown";
            String pName = null;

            if (user != null) {
                userName = user.getName();
                userRole = user.getRole();
                if (user.getAssignedPaathshalaId() != null) {
                    Paathshaala p = paathshaalaMap.get(user.getAssignedPaathshalaId());
                    if (p != null) {
                        pName = p.getName();
                    }
                }
            }

            return new AdminLiveLocationResponse(
                    ping.getUserId(),
                    userName,
                    userRole,
                    pName,
                    ping.getLat(),
                    ping.getLng(),
                    ping.getCapturedAt(),
                    ping.getReceivedAt(),
                    ping.getSyncStatus()
            );
        }).collect(Collectors.toList());
    }

    /**
     * GET /api/admin/users/{userId}/locations/detail
     * Gets a single user's detailed location track for a specific date,
     * including distance to a target paathshaala.
     */
    @GetMapping("/users/{userId}/locations/detail")
    public com.pathshala.stub.dto.UserLocationDetailResponse getUserLocationDetail(
            @org.springframework.web.bind.annotation.PathVariable("userId") java.util.UUID userId,
            @org.springframework.web.bind.annotation.RequestParam("date") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            @org.springframework.web.bind.annotation.RequestParam(value = "paathshaala_id", required = false) java.util.UUID paathshaalaIdParam) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        java.time.ZoneId istZone = java.time.ZoneId.of("Asia/Kolkata");
        java.time.Instant fromInstant = date.atStartOfDay(istZone).toInstant();
        java.time.Instant toInstant = date.atTime(java.time.LocalTime.MAX).atZone(istZone).toInstant();

        List<LocationPoint> pings = locationRepository.findByUserIdAndRange(userId, fromInstant, toInstant);
        
        // Ensure sorted by captured_at ASC
        pings.sort(java.util.Comparator.comparing(LocationPoint::getCapturedAt));

        java.util.UUID targetPaathshaalaId = null;
        if ("teacher".equals(user.getRole())) {
            targetPaathshaalaId = user.getAssignedPaathshalaId();
        } else if ("supervisor".equals(user.getRole())) {
            targetPaathshaalaId = paathshaalaIdParam;
        }

        Paathshaala targetPaathshaala = null;
        if (targetPaathshaalaId != null) {
            targetPaathshaala = paathshalaRepository.findById(targetPaathshaalaId).orElse(null);
        }

        String pName = targetPaathshaala != null ? targetPaathshaala.getName() : null;
        Paathshaala finalTarget = targetPaathshaala;

        List<com.pathshala.stub.dto.LocationPointDetailDto> points = pings.stream().map(ping -> {
            Double distance = null;
            if (finalTarget != null && finalTarget.getLatitude() != null && finalTarget.getLongitude() != null) {
                distance = com.pathshala.stub.util.GeoUtils.haversineMeters(
                        ping.getLat(), ping.getLng(),
                        finalTarget.getLatitude(), finalTarget.getLongitude());
            }
            return new com.pathshala.stub.dto.LocationPointDetailDto(
                    ping.getCapturedAt(),
                    ping.getReceivedAt(),
                    ping.getLat(),
                    ping.getLng(),
                    distance
            );
        }).collect(Collectors.toList());

        return new com.pathshala.stub.dto.UserLocationDetailResponse(
                user.getId(),
                user.getName(),
                user.getRole(),
                date.toString(),
                targetPaathshaalaId,
                pName,
                points
        );
    }

    /**
     * POST /api/admin/locations/request/{userId}
     * Requests an on-demand location refresh from the given user's field app.
     */
    @org.springframework.web.bind.annotation.PostMapping("/locations/request/{userId}")
    public org.springframework.http.ResponseEntity<?> requestOnDemandLocation(
            @org.springframework.web.bind.annotation.PathVariable("userId") java.util.UUID userId) {
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        
        user.setOnDemandRequestedAt(java.time.Instant.now());
        userRepository.save(user);
        
        return org.springframework.http.ResponseEntity.ok(Map.of("status", "requested", "userId", userId));
    }

    /**
     * POST /api/admin/export
     * Generates a CSV export of location pings within the given date range (and optionally filtered by userId).
     */
    @org.springframework.web.bind.annotation.PostMapping(value = "/export", produces = "text/csv")
    public org.springframework.http.ResponseEntity<String> exportLocations(
            @org.springframework.web.bind.annotation.RequestBody com.pathshala.stub.dto.ExportRequest request) {

        java.time.ZoneId istZone = java.time.ZoneId.of("Asia/Kolkata");
        java.time.Instant fromInstant = (request.startDate() != null)
                ? request.startDate().atStartOfDay(istZone).toInstant()
                : null;
        java.time.Instant toInstant = (request.endDate() != null)
                ? request.endDate().atTime(java.time.LocalTime.MAX).atZone(istZone).toInstant()
                : null;

        List<LocationPoint> pings = locationRepository.findByUserIdAndRange(request.userId(), fromInstant, toInstant);

        // Batch fetch users to enrich
        Set<java.util.UUID> userIds = pings.stream().map(LocationPoint::getUserId).collect(Collectors.toSet());
        Map<java.util.UUID, User> userMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        StringBuilder csv = new StringBuilder();
        csv.append("Name,Role,Phone Number,Captured At,Received At,Latitude,Longitude,Sync Status\n");

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(istZone);

        for (LocationPoint ping : pings) {
            User user = userMap.get(ping.getUserId());
            String name = (user != null) ? user.getName() : "Unknown";
            String role = (user != null) ? user.getRole() : "Unknown";
            String phone = (user != null) ? user.getPhoneNumber() : "Unknown";

            // Escape quotes in name/role/phone if necessary (CSV formatting)
            name = "\"" + name.replace("\"", "\"\"") + "\"";
            role = "\"" + role.replace("\"", "\"\"") + "\"";
            phone = "\"" + phone.replace("\"", "\"\"") + "\"";

            String capturedAt = formatter.format(ping.getCapturedAt());
            String receivedAt = formatter.format(ping.getReceivedAt());

            csv.append(String.format("%s,%s,%s,%s,%s,%f,%f,%s\n",
                    name, role, phone, capturedAt, receivedAt, ping.getLat(), ping.getLng(), ping.getSyncStatus()));
        }

        String startStr = (request.startDate() != null) ? request.startDate().toString() : "all";
        String endStr = (request.endDate() != null) ? request.endDate().toString() : "all";
        String filename = "location-export-" + startStr + "-to-" + endStr + ".csv";

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csv.toString());
    }
}
