package com.pathshala.stub.controller;

import com.pathshala.stub.entity.SystemConfig;
import com.pathshala.stub.repository.SystemConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/system-config")
public class AdminSystemConfigController {

    private final SystemConfigRepository systemConfigRepository;

    public AdminSystemConfigController(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    @GetMapping("/healing-interval")
    public ResponseEntity<?> getHealingInterval() {
        int minutes = systemConfigRepository.findById("healing_check_interval_minutes")
                .map(sc -> Integer.parseInt(sc.getValue()))
                .orElse(30);
        return ResponseEntity.ok(Map.of("minutes", minutes));
    }

    @PutMapping("/healing-interval")
    public ResponseEntity<?> updateHealingInterval(@RequestBody Integer minutes) {
        if (minutes == null || minutes < 1) {
            return ResponseEntity.badRequest().body(Map.of("error", "minutes must be a positive integer"));
        }

        SystemConfig sc = systemConfigRepository.findById("healing_check_interval_minutes")
                .orElse(new SystemConfig("healing_check_interval_minutes", "30"));
        
        sc.setValue(String.valueOf(minutes));
        systemConfigRepository.save(sc);
        
        return ResponseEntity.ok(Map.of("minutes", minutes));
    }
}
