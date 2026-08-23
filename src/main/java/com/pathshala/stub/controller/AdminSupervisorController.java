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

    public AdminSupervisorController(UserService userService) {
        this.userService = userService;
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
}
