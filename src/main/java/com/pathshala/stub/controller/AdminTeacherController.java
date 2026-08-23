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
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {

    private final UserService userService;

    public AdminTeacherController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TeacherResponse> create(
            @Valid @RequestBody CreateTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createTeacher(request));
    }

    @GetMapping
    public PagedResponse<TeacherResponse> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userService.findAllTeachers(pageable);
    }

    @GetMapping("/{id}")
    public TeacherResponse getById(@PathVariable UUID id) {
        return userService.findTeacherById(id);
    }

    @PutMapping("/{id}")
    public TeacherResponse update(
            @PathVariable UUID id,
            @RequestBody UpdateTeacherRequest request) {
        return userService.updateTeacher(id, request);
    }

    /** Soft-delete: sets active=false. Does not hard-delete the row. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.deactivateTeacher(id);
        return ResponseEntity.noContent().build();
    }
}
