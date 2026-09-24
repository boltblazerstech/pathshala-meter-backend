package com.pathshala.stub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "geofence_events")
public class GeofenceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lng", nullable = false)
    private double lng;

    @Column(name = "paathshaala_id")
    private UUID paathshaalaId;

    public GeofenceEvent() {}

    public GeofenceEvent(UUID userId, String eventType, Instant eventTime, LocalDate eventDate, double lat, double lng, UUID paathshaalaId) {
        this.userId = userId;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.eventDate = eventDate;
        this.lat = lat;
        this.lng = lng;
        this.paathshaalaId = paathshaalaId;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getEventType() { return eventType; }
    public Instant getEventTime() { return eventTime; }
    public LocalDate getEventDate() { return eventDate; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public UUID getPaathshaalaId() { return paathshaalaId; }

    // Setters omitted for brevity since these are mostly immutable events, but adding them for JPA
    public void setId(UUID id) { this.id = id; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setEventTime(Instant eventTime) { this.eventTime = eventTime; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setPaathshaalaId(UUID paathshaalaId) { this.paathshaalaId = paathshaalaId; }
}
