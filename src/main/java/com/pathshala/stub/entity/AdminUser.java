package com.pathshala.stub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_users")
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 1;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId()           { return id; }
    public String getEmail()      { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
    public int getTokenVersion()  { return tokenVersion; }

    public void setTokenVersion(int tokenVersion) { this.tokenVersion = tokenVersion; }
}
