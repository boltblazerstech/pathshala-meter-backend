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

    @GetMapping("/history")
    public List<AdminLiveLocationResponse> getLocationHistory(
            @org.springframework.web.bind.annotation.RequestParam("userId") java.util.UUID userId,
            @org.springframework.web.bind.annotation.RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        
        java.time.Instant from = null;
        java.time.Instant to = null;
        
        if (date != null) {
            java.time.ZoneId istZone = java.time.ZoneId.of("Asia/Kolkata");
            from = date.atStartOfDay(istZone).toInstant();
            to = date.plusDays(1).atStartOfDay(istZone).toInstant();
        }

        List<LocationPoint> history = locationRepository.findByUserIdAndRange(userId, from, to);
        if (history.isEmpty()) return List.of();

        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getName() : "Unknown";
        String userRole = user != null ? user.getRole() : "Unknown";
        String pName = null;

        if (user != null && user.getAssignedPaathshalaId() != null) {
            pName = paathshalaRepository.findById(user.getAssignedPaathshalaId())
                    .map(Paathshaala::getName).orElse(null);
        }
        
        final String finalUserName = userName;
        final String finalUserRole = userRole;
        final String finalPName = pName;

        return history.stream().map(ping -> new AdminLiveLocationResponse(
                ping.getUserId(),
                finalUserName,
                finalUserRole,
                finalPName,
                ping.getLat(),
                ping.getLng(),
                ping.getCapturedAt(),
                ping.getReceivedAt(),
                ping.getSyncStatus()
        )).collect(Collectors.toList());
    }
}
