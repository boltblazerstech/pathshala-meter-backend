package com.pathshala.stub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Maps to location_pings table.
 * No Jackson annotations here — serialization is the responsibility of
 * LocationPingResponse (the response DTO), not the JPA entity.
 */
@Entity
@Table(name = "location_pings")
public class LocationPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "sync_status", nullable = false)
    private String syncStatus = "synced";

    @PrePersist
    protected void onCreate() {
        receivedAt = Instant.now();
    }

    public Long    getId()          { return id; }
    public UUID    getUserId()      { return userId; }
    public double  getLat()         { return lat; }
    public double  getLng()         { return lng; }
    public Instant getCapturedAt()  { return capturedAt; }
    public Instant getReceivedAt()  { return receivedAt; }
    public String  getSyncStatus()  { return syncStatus; }

    public void setUserId(UUID userId)             { this.userId    = userId; }
    public void setLat(double lat)                 { this.lat       = lat; }
    public void setLng(double lng)                 { this.lng       = lng; }
    public void setCapturedAt(Instant capturedAt)  { this.capturedAt = capturedAt; }
    public void setSyncStatus(String syncStatus)   { this.syncStatus = syncStatus; }
}
