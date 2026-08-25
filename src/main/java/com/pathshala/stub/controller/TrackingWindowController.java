package com.pathshala.stub.controller;

import com.pathshala.stub.dto.TrackingWindowResponse;
import com.pathshala.stub.service.TrackingWindowService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Field-app tracking-window endpoint.
 *
 * Resolution order:
 *  1. If ?start= / ?end= / ?interval= query params are present, use them
 *     directly (testing override — lets testers set a 10-min window without
 *     waiting for a real multi-hour window every cycle).
 *  2. Otherwise, look up the caller's most-recent active window from the DB
 *     (effective_from_date <= today IST).
 *  3. If no DB row exists yet, fall back to application.properties defaults
 *     so the app works before any admin has configured windows.
 */
@RestController
@RequestMapping("/api/tracking-window")
public class TrackingWindowController {

    private final TrackingWindowService trackingWindowService;
    private final com.pathshala.stub.repository.SystemConfigRepository systemConfigRepository;

    @Value("${tracking.default-start:09:00}")
    private String defaultStart;

    @Value("${tracking.default-end:17:00}")
    private String defaultEnd;

    @Value("${tracking.default-interval:15}")
    private int defaultInterval;

    public TrackingWindowController(TrackingWindowService trackingWindowService,
                                    com.pathshala.stub.repository.SystemConfigRepository systemConfigRepository) {
        this.trackingWindowService = trackingWindowService;
        this.systemConfigRepository = systemConfigRepository;
    }

    private int getHealingInterval() {
        return systemConfigRepository.findById("healing_check_interval_minutes")
                .map(sc -> Integer.parseInt(sc.getValue()))
                .orElse(30);
    }

    @GetMapping
    public ResponseEntity<TrackingWindowResponse> getWindow(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) Integer interval) {

        int healingInterval = getHealingInterval();

        // ── 1. Testing query-param override ────────────────────────────────
        if (start != null || end != null || interval != null) {
            return ResponseEntity.ok(new TrackingWindowResponse(
                    start    != null ? start    : defaultStart,
                    end      != null ? end      : defaultEnd,
                    interval != null ? interval : defaultInterval,
                    healingInterval
            ));
        }

        // ── 2. Real DB lookup for the calling user's window ────────────────
        UUID userId = UUID.fromString(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return trackingWindowService.findCurrentForUser(userId)
                .map(resp -> new TrackingWindowResponse(resp.startTime(), resp.endTime(), resp.intervalMinutes(), healingInterval))
                .map(ResponseEntity::ok)
                // ── 3. Fall back to application.properties defaults ────────
                .orElse(ResponseEntity.ok(
                        new TrackingWindowResponse(defaultStart, defaultEnd, defaultInterval, healingInterval)));
    }
}
