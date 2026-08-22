package com.pathshala.stub.controller;

import com.pathshala.stub.dto.ResetFieldPasswordRequest;
import com.pathshala.stub.entity.SystemConfig;
import com.pathshala.stub.repository.SystemConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin-only system configuration endpoints.
 * Requires Authorization: Bearer <admin-JWT> (enforced by SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/system-config")
public class AdminSystemConfigController {

    private static final String FIELD_PASSWORD_KEY = "field_app_shared_password";

    private final SystemConfigRepository systemConfigRepository;
    private final PasswordEncoder        passwordEncoder;

    public AdminSystemConfigController(SystemConfigRepository systemConfigRepository,
                                       PasswordEncoder passwordEncoder) {
        this.systemConfigRepository = systemConfigRepository;
        this.passwordEncoder        = passwordEncoder;
    }

    /**
     * Rotate the shared password used by all field-app users.
     * Bcrypt-hashes the new password and saves it to system_config.
     * No deploy needed — takes effect immediately on the next login.
     */
    @PostMapping("/reset-field-password")
    public ResponseEntity<?> resetFieldPassword(@RequestBody ResetFieldPasswordRequest request) {
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "new_password must not be blank"));
        }

        String newHash = passwordEncoder.encode(request.newPassword());

        SystemConfig config = systemConfigRepository
                .findById(FIELD_PASSWORD_KEY)
                .orElse(new SystemConfig(FIELD_PASSWORD_KEY, null));
        config.setValue(newHash);
        systemConfigRepository.save(config);

        return ResponseEntity.ok(Map.of("message", "Field app password updated successfully"));
    }
}
