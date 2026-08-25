package com.pathshala.stub.security;

import com.pathshala.stub.entity.AdminUser;
import com.pathshala.stub.entity.User;
import com.pathshala.stub.repository.AdminUserRepository;
import com.pathshala.stub.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   @Lazy UserRepository userRepository,
                                   @Lazy AdminUserRepository adminUserRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.isTokenValid(token)) {
                // sub = user_id or admin_id (UUID string)
                String subject = jwtUtil.extractSubject(token);
                // role = "admin" | "supervisor" | "teacher"
                String role    = jwtUtil.extractClaim(token, "role");
                
                String tokenVersionStr = jwtUtil.extractClaim(token, "token_version");

                if (subject != null && role != null && tokenVersionStr != null) {
                    try {
                        int tokenVersion = Integer.parseInt(tokenVersionStr);
                        boolean isValidVersion = false;
                        
                        UUID subjectId = UUID.fromString(subject);
                        
                        if ("admin".equals(role)) {
                            AdminUser admin = adminUserRepository.findById(subjectId).orElse(null);
                            if (admin != null && admin.getTokenVersion() == tokenVersion) {
                                isValidVersion = true;
                            }
                        } else {
                            User user = userRepository.findById(subjectId).orElse(null);
                            if (user != null && user.getTokenVersion() == tokenVersion && user.isActive()) {
                                isValidVersion = true;
                            }
                        }

                        if (isValidVersion) {
                            // Spring Security expects authorities prefixed with ROLE_
                            var authority = new SimpleGrantedAuthority("ROLE_" + role);
                            var auth = new UsernamePasswordAuthenticationToken(
                                    subject, null, List.of(authority));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    } catch (Exception e) {
                        // Ignore malformed tokens or UUIDs
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
