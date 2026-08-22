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
                "role",     "admin",
                "admin_id", admin.getId().toString()
        );
        String token = jwtUtil.generateToken(admin.getId().toString(), claims);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    private static ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
    }
}
