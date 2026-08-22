package com.pathshala.stub.controller;

import com.pathshala.stub.dto.*;
import com.pathshala.stub.service.PaathshalaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/paathshaalas")
public class AdminPaathshalaController {

    private final PaathshalaService paathshalaService;

    public AdminPaathshalaController(PaathshalaService paathshalaService) {
        this.paathshalaService = paathshalaService;
    }

    @PostMapping
    public ResponseEntity<PaathshalaResponse> create(
            @Valid @RequestBody CreatePaathshalaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paathshalaService.create(request));
    }

    @GetMapping
    public PagedResponse<PaathshalaResponse> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return paathshalaService.findAll(pageable);
    }

    @PutMapping("/{id}")
    public PaathshalaResponse update(
            @PathVariable UUID id,
            @RequestBody UpdatePaathshalaRequest request) {
        return paathshalaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        paathshalaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
