package com.pathshala.stub.controller;

import com.pathshala.stub.dto.*;
import com.pathshala.stub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/supervisors")
public class AdminSupervisorController {

    private final UserService userService;
    private final com.pathshala.stub.service.PaathshalaService paathshalaService;

    private final com.pathshala.stub.service.SupervisorTimetableService timetableService;

    public AdminSupervisorController(UserService userService, 
                                     com.pathshala.stub.service.PaathshalaService paathshalaService,
                                     com.pathshala.stub.service.SupervisorTimetableService timetableService) {
        this.userService = userService;
        this.paathshalaService = paathshalaService;
        this.timetableService = timetableService;
    }

    @PostMapping
    public ResponseEntity<SupervisorResponse> create(
            @Valid @RequestBody CreateSupervisorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createSupervisor(request));
    }

    @GetMapping
    public PagedResponse<SupervisorResponse> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userService.findAllSupervisors(pageable);
    }

    @GetMapping("/{id}")
    public SupervisorResponse getById(@PathVariable UUID id) {
        return userService.findSupervisorById(id);
    }

    @PutMapping("/{id}")
    public SupervisorResponse update(
            @PathVariable UUID id,
            @RequestBody UpdateSupervisorRequest request) {
        return userService.updateSupervisor(id, request);
    }

    /** Soft-delete: sets active=false. Does not hard-delete the row. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.deactivateSupervisor(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/selected-paathshaala")
    public SupervisorResponse updateSelectedPaathshaala(
            @PathVariable UUID id,
            @RequestBody UpdateSelectedPaathshaalaRequest request) {
        return userService.updateSelectedPaathshaala(id, request);
    }

    @GetMapping("/{id}/paathshaalas")
    public java.util.List<PaathshalaResponse> getPaathshaalas(@PathVariable UUID id) {
        return paathshalaService.findBySupervisorId(id);
    }

    // ── Timetable Endpoints ───────────────────────────────────────────

    @GetMapping("/{id}/timetable")
    public TimetableResponse getTimetable(@PathVariable UUID id) {
        return timetableService.getTimetable(id);
    }

    @PutMapping("/{id}/timetable")
    public TimetableResponse updateTimetable(
            @PathVariable UUID id,
            @RequestBody TimetableResponse request) {
        return timetableService.updateTimetable(id, request);
    }

    @PostMapping("/{id}/timetable/overrides")
    public TimetableOverrideDto setOverride(
            @PathVariable UUID id,
            @RequestBody TimetableOverrideDto request) {
        return timetableService.setOverride(id, request);
    }

    @DeleteMapping("/{id}/timetable/overrides")
    public ResponseEntity<Void> deleteOverride(
            @PathVariable UUID id,
            @RequestBody TimetableOverrideDeleteRequest request) {
        timetableService.deleteOverride(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/timetable/overrides")
    public java.util.List<TimetableOverrideDto> getOverrides(
            @PathVariable UUID id,
            @RequestParam java.time.LocalDate from,
            @RequestParam java.time.LocalDate to) {
        return timetableService.getOverrides(id, from, to);
    }
}
