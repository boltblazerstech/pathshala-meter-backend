package com.pathshala.stub.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "supervisor_timetable_overrides", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"supervisor_id", "override_date", "slot"})
})
public class SupervisorTimetableOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "supervisor_id", nullable = false)
    private UUID supervisorId;

    @Column(name = "override_date", nullable = false)
    private LocalDate overrideDate;

    @Column(name = "slot", nullable = false, length = 10)
    private String slot;

    @Column(name = "paathshaala_id")
    private UUID paathshaalaId;

    public SupervisorTimetableOverride() {}

    public SupervisorTimetableOverride(UUID supervisorId, LocalDate overrideDate, String slot, UUID paathshaalaId) {
        this.supervisorId = supervisorId;
        this.overrideDate = overrideDate;
        this.slot = slot;
        this.paathshaalaId = paathshaalaId;
    }

    public UUID getId() { return id; }
    public UUID getSupervisorId() { return supervisorId; }
    public LocalDate getOverrideDate() { return overrideDate; }
    public String getSlot() { return slot; }
    public UUID getPaathshaalaId() { return paathshaalaId; }

    public void setPaathshaalaId(UUID paathshaalaId) { this.paathshaalaId = paathshaalaId; }
    public void setSupervisorId(UUID supervisorId) { this.supervisorId = supervisorId; }
    public void setOverrideDate(LocalDate overrideDate) { this.overrideDate = overrideDate; }
    public void setSlot(String slot) { this.slot = slot; }
}
