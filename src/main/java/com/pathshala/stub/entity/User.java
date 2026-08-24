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

    @Column(name = "selected_paathshaala_id")
    private UUID selectedPaathshaalaId;

    @Column(name = "password")
    private String password;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "on_demand_requested_at")
    private Instant onDemandRequestedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // ── Getters ──────────────────────────────────────────────────────
    public UUID getId()                    { return id; }
    public String getName()               { return name; }
    public String getPhoneNumber()        { return phoneNumber; }
    public String getRole()               { return role; }
    public UUID getAssignedPaathshalaId()  { return assignedPaathshalaId; }
    public UUID getSelectedPaathshaalaId() { return selectedPaathshaalaId; }
    public String getPassword()            { return password; }
    public boolean isActive()              { return active; }
    public Instant getCreatedAt()          { return createdAt; }
    public Instant getOnDemandRequestedAt() { return onDemandRequestedAt; }

    // ── Setters (needed for create and update operations) ─────────────
    public void setName(String name)                                 { this.name = name; }
    public void setPhoneNumber(String phoneNumber)                   { this.phoneNumber = phoneNumber; }
    public void setRole(String role)                                 { this.role = role; }
    public void setAssignedPaathshalaId(UUID assignedPaathshalaId)   { this.assignedPaathshalaId = assignedPaathshalaId; }
    public void setSelectedPaathshaalaId(UUID selectedPaathshaalaId) { this.selectedPaathshaalaId = selectedPaathshaalaId; }
    public void setPassword(String password)                         { this.password = password; }
    public void setActive(boolean active)                            { this.active = active; }
    public void setOnDemandRequestedAt(Instant onDemandRequestedAt)  { this.onDemandRequestedAt = onDemandRequestedAt; }
}
