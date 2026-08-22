package com.pathshala.stub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    /** Values: "supervisor" | "teacher" — enforced by DB CHECK constraint. */
    @Column(nullable = false)
    private String role;

    @Column(name = "assigned_paathshaala_id")
    private UUID assignedPaathshalaId;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId()                    { return id; }
    public String getName()               { return name; }
    public String getPhoneNumber()        { return phoneNumber; }
    public String getRole()               { return role; }
    public UUID getAssignedPaathshalaId() { return assignedPaathshalaId; }
    public boolean isActive()             { return active; }
    public Instant getCreatedAt()         { return createdAt; }
}
