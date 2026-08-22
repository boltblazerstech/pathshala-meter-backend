package com.pathshala.stub.controller;

import com.pathshala.stub.dto.LoginRequest;
import com.pathshala.stub.dto.LoginResponse;
import com.pathshala.stub.entity.SystemConfig;
import com.pathshala.stub.entity.User;
import com.pathshala.stub.repository.SystemConfigRepository;
import com.pathshala.stub.repository.UserRepository;
import com.pathshala.stub.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Field-app login: phone_number + shared password.
 * The shared password is NOT per-user; it is a single bcrypt hash
 * stored in system_config keyed 'field_app_shared_password'.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository         userRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtUtil                jwtUtil;

    public AuthController(UserRepository userRepository,
                          SystemConfigRepository systemConfigRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository         = userRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.passwordEncoder        = passwordEncoder;
        this.jwtUtil                = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // 1. Find active user by phone number
        User user = userRepository
                .findByPhoneNumberAndActiveTrue(request.phoneNumber())
                .orElse(null);
        if (user == null) {
            return unauthorized();
        }

        // 2. Load shared password hash from system_config
        SystemConfig config = systemConfigRepository
                .findById("field_app_shared_password")
                .orElse(null);
        if (config == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server misconfiguration: shared password not set"));
        }

        // 3. BCrypt verify — shared password, not per-user
        if (!passwordEncoder.matches(request.password(), config.getValue())) {
            return unauthorized();
        }

        // 4. Build JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",    user.getRole());
        claims.put("user_id", user.getId().toString());
        if (user.getAssignedPaathshalaId() != null) {
            claims.put("assigned_paathshaala_id", user.getAssignedPaathshalaId().toString());
        }

        String token = jwtUtil.generateToken(user.getId().toString(), claims);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    private static ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
    }
}
