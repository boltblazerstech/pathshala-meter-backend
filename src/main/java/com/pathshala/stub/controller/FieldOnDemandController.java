package com.pathshala.stub.controller;

import com.pathshala.stub.entity.User;
import com.pathshala.stub.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/field")
public class FieldOnDemandController {

    private final UserRepository userRepository;

    public FieldOnDemandController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UUID currentUserId() {
        String subject = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(subject);
    }

    /**
     * GET /api/field/on-demand-check
     * Lightweight poll to check if admin requested an on-demand location refresh.
     */
    @GetMapping("/on-demand-check")
    public ResponseEntity<?> checkOnDemand() {
        User user = userRepository.findById(currentUserId()).orElse(null);
        if (user == null || user.getOnDemandRequestedAt() == null) {
            return ResponseEntity.ok(Map.of("pending", false));
        }

        return ResponseEntity.ok(Map.of(
                "pending", true,
                "requested_at", user.getOnDemandRequestedAt().toString()
        ));
    }

    /**
     * POST /api/field/on-demand-complete
     * Field app calls this after successfully queuing the requested on-demand location ping.
     */
    @PostMapping("/on-demand-complete")
    public ResponseEntity<?> completeOnDemand() {
        User user = userRepository.findById(currentUserId()).orElse(null);
        if (user != null && user.getOnDemandRequestedAt() != null) {
            user.setOnDemandRequestedAt(null);
            userRepository.save(user);
        }
        return ResponseEntity.ok(Map.of("status", "cleared"));
    }
}
