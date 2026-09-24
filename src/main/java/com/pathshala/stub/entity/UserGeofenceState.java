package com.pathshala.stub.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_geofence_state")
public class UserGeofenceState {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "state_date", nullable = false)
    private LocalDate stateDate;

    @Column(name = "is_at_home", nullable = false)
    private boolean isAtHome;

    @Column(name = "is_at_school", nullable = false)
    private boolean isAtSchool;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    public UserGeofenceState() {}

    public UserGeofenceState(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() { return userId; }
    public LocalDate getStateDate() { return stateDate; }
    public boolean isAtHome() { return isAtHome; }
    public boolean isAtSchool() { return isAtSchool; }
    public Instant getLastUpdated() { return lastUpdated; }

    public void setUserId(UUID userId) { this.userId = userId; }
    public void setStateDate(LocalDate stateDate) { this.stateDate = stateDate; }
    public void setAtHome(boolean atHome) { isAtHome = atHome; }
    public void setAtSchool(boolean atSchool) { isAtSchool = atSchool; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
