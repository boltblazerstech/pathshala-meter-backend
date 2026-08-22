package com.pathshala.stub.controller;

import com.pathshala.stub.dto.*;
import com.pathshala.stub.service.TrackingWindowService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/tracking-windows")
public class AdminTrackingWindowController {

    private final TrackingWindowService service;

    public AdminTrackingWindowController(TrackingWindowService service) {
        this.service = service;
    }

    /** Create a window for a single user. */
    @PostMapping
    public ResponseEntity<AdminTrackingWindowResponse> create(
            @Valid @RequestBody CreateTrackingWindowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /**
     * Bulk-apply the same window definition to multiple users.
     * All-or-nothing: if any user_id is invalid the whole request fails.
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<AdminTrackingWindowResponse>> createBulk(
            @Valid @RequestBody BulkTrackingWindowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBulk(request));
    }

    /**
     * Admin table view: every user's currently-effective window as of a date.
     * Defaults to today (IST) when no date is supplied.
     *
     * @param date optional YYYY-MM-DD, defaults to today
     */
    @GetMapping
    public List<EffectiveWindowEntry> listEffective(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        return service.findAllEffectiveAsOf(effectiveDate);
    }

    /** Update any field of an existing window. */
    @PutMapping("/{id}")
    public AdminTrackingWindowResponse update(
            @PathVariable UUID id,
            @RequestBody UpdateTrackingWindowRequest request) {
        return service.update(id, request);
    }
}
