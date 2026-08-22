package com.pathshala.stub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "tracking_windows")
public class TrackingWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "interval_minutes", nullable = false)
    private int intervalMinutes;

    @Column(name = "effective_from_date", nullable = false)
    private LocalDate effectiveFromDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // ── Getters ──────────────────────────────────────────────────────
    public UUID      getId()                { return id; }
    public UUID      getUserId()            { return userId; }
    public LocalTime getStartTime()         { return startTime; }
    public LocalTime getEndTime()           { return endTime; }
    public int       getIntervalMinutes()   { return intervalMinutes; }
    public LocalDate getEffectiveFromDate() { return effectiveFromDate; }
    public boolean   isActive()             { return isActive; }
    public Instant   getCreatedAt()         { return createdAt; }

    // ── Setters ──────────────────────────────────────────────────────
    public void setUserId(UUID userId)                      { this.userId = userId; }
    public void setStartTime(LocalTime startTime)           { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime)               { this.endTime = endTime; }
    public void setIntervalMinutes(int intervalMinutes)     { this.intervalMinutes = intervalMinutes; }
    public void setEffectiveFromDate(LocalDate d)           { this.effectiveFromDate = d; }
    public void setActive(boolean isActive)                 { this.isActive = isActive; }
}
