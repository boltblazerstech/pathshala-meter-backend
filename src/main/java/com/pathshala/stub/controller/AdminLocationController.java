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
@RequestMapping("/api/admin/locations")
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
    @GetMapping("/live")
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
    @GetMapping
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
}
