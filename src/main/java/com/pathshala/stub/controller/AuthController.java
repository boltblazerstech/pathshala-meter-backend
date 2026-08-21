package com.pathshala.stub.controller;

import com.pathshala.stub.dto.LoginRequest;
import com.pathshala.stub.dto.LoginResponse;
import com.pathshala.stub.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if ("9999999999".equals(request.phoneNumber()) && "test1234".equals(request.password())) {
            String token = jwtUtil.generateToken("user-001");
            return ResponseEntity.ok(new LoginResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
    }
}
