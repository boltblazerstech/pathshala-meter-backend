package com.pathshala.stub.controller;

import com.pathshala.stub.dto.BatchReceiveResponse;
import com.pathshala.stub.dto.LocationPingResponse;
import com.pathshala.stub.dto.LocationPointDto;
import com.pathshala.stub.entity.LocationPoint;
import com.pathshala.stub.repository.LocationPointRepository;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final LocationPointRepository repository;

    public LocationController(LocationPointRepository repository) {
        this.repository = repository;
    }

    /**
     * POST /api/locations/batch
     *
     * user_id is taken from the JWT claim, not the request body.
     * received_at is set server-side via @PrePersist — never trusted from client.
     */
    @PostMapping("/batch")
    public BatchReceiveResponse batchReceive(
            @Valid @RequestBody List<@Valid LocationPointDto> dtos) {

        UUID userId = currentUserId();

        List<LocationPoint> points = dtos.stream().map(dto -> {
            Instant capturedAt = parseCapturedAt(dto.capturedAt());
            LocationPoint point = new LocationPoint();
            point.setUserId(userId);
            point.setLat(dto.lat());
            point.setLng(dto.lng());
            point.setCapturedAt(capturedAt);
            // receivedAt and syncStatus are set by @PrePersist / column default
            return point;
        }).collect(Collectors.toList());

        repository.saveAll(points);
        return new BatchReceiveResponse(points.size());
    }

    /**
     * GET /api/locations
     *
     * user_id is mandatory — supervisors can look up any user; teachers
     * can only see their own (enforced by the calling convention; the JWT
     * always supplies the caller's own ID which the Flutter app sends as user_id).
     *
     * Optional date-range filter: from / to are local IST dates (YYYY-MM-DD).
     * They are converted to start-of-day / end-of-day Instants in IST before
     * querying so the caller doesn't need to know about UTC offsets.
     *
     * Examples:
     *   GET /api/locations?user_id=<uuid>
     *   GET /api/locations?user_id=<uuid>&from=2026-08-20
     *   GET /api/locations?user_id=<uuid>&from=2026-08-20&to=2026-08-22
     */
    @GetMapping
    public ResponseEntity<?> getLocations(
            @RequestParam("user_id") UUID userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from != null && to != null && from.isAfter(to)) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "'from' date must not be after 'to' date"));
        }

        // Convert IST local dates to UTC Instants for the DB query
        Instant fromInstant = (from != null)
                ? from.atStartOfDay(IST).toInstant()
                : null;
        Instant toInstant = (to != null)
                ? to.atTime(LocalTime.MAX).atZone(IST).toInstant()
                : null;

        List<LocationPingResponse> results = repository
                .findByUserIdAndRange(userId, fromInstant, toInstant)
                .stream()
                .map(LocationPingResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    // ── Helpers ────────────────────────────────────────────────────────

    /** JWT filter sets the principal to the user's UUID string (sub claim). */
    private UUID currentUserId() {
        String subject = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return UUID.fromString(subject);
    }

    private Instant parseCapturedAt(String raw) {
        try {
            return Instant.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "captured_at must be an ISO-8601 instant (e.g. '2026-08-22T10:00:00Z'), got: '"
                            + raw + "'");
        }
    }
}
