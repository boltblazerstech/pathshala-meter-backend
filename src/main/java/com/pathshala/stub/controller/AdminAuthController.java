package com.pathshala.stub.controller;

import com.pathshala.stub.dto.AdminLoginRequest;
import com.pathshala.stub.dto.LoginResponse;
import com.pathshala.stub.entity.AdminUser;
import com.pathshala.stub.repository.AdminUserRepository;
import com.pathshala.stub.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Admin login: email + bcrypt-hashed password stored in admin_users table.
 * Issues a JWT with role="admin" and admin_id claims.
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtUtil             jwtUtil;

    public AdminAuthController(AdminUserRepository adminUserRepository,
                               PasswordEncoder passwordEncoder,
                               JwtUtil jwtUtil) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder     = passwordEncoder;
        this.jwtUtil             = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequest request) {

        // 1. Look up admin by email
        AdminUser admin = adminUserRepository.findByEmail(request.email()).orElse(null);
        if (admin == null) {
            return unauthorized();
        }

        // 2. BCrypt verify the per-admin password hash
        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            return unauthorized();
        }

        // 3. Issue admin JWT
        Map<String, Object> claims = Map.of(
                "role",          "admin",
                "admin_id",      admin.getId().toString(),
                "token_version", admin.getTokenVersion()
        );
        String token = jwtUtil.generateToken(admin.getId().toString(), claims);
        return ResponseEntity.ok(new LoginResponse(token, admin.getEmail()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        String subject = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AdminUser admin = adminUserRepository.findById(UUID.fromString(subject)).orElse(null);
        if (admin == null) {
            return unauthorized();
        }
        return ResponseEntity.ok(Map.of(
                "id", admin.getId(),
                "email", admin.getEmail(),
                "role", "admin"
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        String subject = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AdminUser admin = adminUserRepository.findById(UUID.fromString(subject)).orElse(null);
        if (admin != null) {
            admin.setTokenVersion(admin.getTokenVersion() + 1);
            adminUserRepository.save(admin);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private static ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
    }
}
