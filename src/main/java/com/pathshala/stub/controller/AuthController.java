package com.pathshala.stub.controller;

import com.pathshala.stub.dto.LoginRequest;
import com.pathshala.stub.dto.LoginResponse;
import com.pathshala.stub.entity.User;
import com.pathshala.stub.repository.UserRepository;
import com.pathshala.stub.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Field-app login: phone_number + per-user plaintext password.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil        jwtUtil;

    public AuthController(UserRepository userRepository,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil        = jwtUtil;
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

        // 2. Plaintext password comparison — per-user, not shared
        if (user.getPassword() == null || !user.getPassword().equals(request.password())) {
            return unauthorized();
        }

        // 3. Save FCM token if provided
        if (request.fcmToken() != null && !request.fcmToken().isBlank()) {
            user.setFcmToken(request.fcmToken());
            userRepository.save(user);
        }

        // 4. Build JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",    user.getRole());
        claims.put("user_id", user.getId().toString());
        claims.put("token_version", user.getTokenVersion());
        claims.put("name", user.getName());
        
        if (user.getAssignedPaathshalaId() != null) {
            claims.put("assigned_paathshaala_id", user.getAssignedPaathshalaId().toString());
        }

        String token = jwtUtil.generateToken(user.getId().toString(), claims);
        return ResponseEntity.ok(new LoginResponse(token, user.getName()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        String subject = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(java.util.UUID.fromString(subject)).orElse(null);
        if (user != null) {
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private static ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
    }
}
