package com.pathshala.stub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paathshaalas")
public class Paathshaala {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private Double latitude;

    private Double longitude;

    @Column(name = "source_map_link")
    private String sourceMapLink;

    private String address;

    /**
     * "parsed"   = coordinate extracted cleanly from the map link.
     * "fallback" = only the viewport center was available — treat with caution.
     */
    @Column(name = "coordinate_confidence", nullable = false)
    private String coordinateConfidence;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "supervisor_id")
    private UUID supervisorId;

    @Column(name = "opening_time")
    private java.time.LocalTime openingTime;

    @Column(name = "closing_time")
    private java.time.LocalTime closingTime;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId()                      { return id; }
    public String getName()                  { return name; }
    public Double getLatitude()              { return latitude; }
    public Double getLongitude()             { return longitude; }
    public String getSourceMapLink()         { return sourceMapLink; }
    public String getAddress()               { return address; }
    public String getCoordinateConfidence()  { return coordinateConfidence; }
    public Instant getCreatedAt()            { return createdAt; }
    public UUID getSupervisorId()            { return supervisorId; }
    public java.time.LocalTime getOpeningTime() { return openingTime; }
    public java.time.LocalTime getClosingTime() { return closingTime; }

    public void setName(String name)                           { this.name = name; }
    public void setLatitude(Double latitude)                   { this.latitude = latitude; }
    public void setLongitude(Double longitude)                 { this.longitude = longitude; }
    public void setSourceMapLink(String sourceMapLink)         { this.sourceMapLink = sourceMapLink; }
    public void setAddress(String address)                     { this.address = address; }
    public void setCoordinateConfidence(String c)              { this.coordinateConfidence = c; }
    public void setSupervisorId(UUID supervisorId)             { this.supervisorId = supervisorId; }
    public void setOpeningTime(java.time.LocalTime openingTime) { this.openingTime = openingTime; }
    public void setClosingTime(java.time.LocalTime closingTime) { this.closingTime = closingTime; }
}
