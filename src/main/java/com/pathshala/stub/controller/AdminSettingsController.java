package com.pathshala.stub.controller;

import com.pathshala.stub.dto.SettingsDto;
import com.pathshala.stub.entity.SystemConfig;
import com.pathshala.stub.repository.SystemConfigRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    private final SystemConfigRepository configRepository;

    public AdminSettingsController(SystemConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @GetMapping
    public SettingsDto getSettings() {
        Map<String, String> configs = configRepository.findAll().stream()
                .collect(Collectors.toMap(SystemConfig::getKey, SystemConfig::getValue));
        
        return new SettingsDto(
                parseInteger(configs.get("fetch_interval_minutes")),
                parseInteger(configs.get("pre_buffer_minutes")),
                parseInteger(configs.get("post_buffer_minutes")),
                parseInteger(configs.get("radius_leave_home_meters")),
                parseInteger(configs.get("radius_reach_school_meters")),
                parseInteger(configs.get("radius_leave_school_meters")),
                parseInteger(configs.get("radius_reach_home_meters"))
        );
    }

    @PutMapping
    @Transactional
    public SettingsDto updateSettings(@RequestBody SettingsDto request) {
        updateConfig("fetch_interval_minutes", request.fetchIntervalMinutes());
        updateConfig("pre_buffer_minutes", request.preBufferMinutes());
        updateConfig("post_buffer_minutes", request.postBufferMinutes());
        updateConfig("radius_leave_home_meters", request.radiusLeaveHomeMeters());
        updateConfig("radius_reach_school_meters", request.radiusReachSchoolMeters());
        updateConfig("radius_leave_school_meters", request.radiusLeaveSchoolMeters());
        updateConfig("radius_reach_home_meters", request.radiusReachHomeMeters());
        
        return getSettings();
    }
    
    private void updateConfig(String key, Integer value) {
        if (value != null) {
            configRepository.save(new SystemConfig(key, value.toString()));
        }
    }

    private Integer parseInteger(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
