package com.pathshala.stub.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "supervisor_timetable", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"supervisor_id", "day_of_week", "slot"})
})
public class SupervisorTimetable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "supervisor_id", nullable = false)
    private UUID supervisorId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "slot", nullable = false, length = 10)
    private String slot;

    @Column(name = "paathshaala_id")
    private UUID paathshaalaId;

    public SupervisorTimetable() {}

    public SupervisorTimetable(UUID supervisorId, Integer dayOfWeek, String slot, UUID paathshaalaId) {
        this.supervisorId = supervisorId;
        this.dayOfWeek = dayOfWeek;
        this.slot = slot;
        this.paathshaalaId = paathshaalaId;
    }

    public UUID getId() { return id; }
    public UUID getSupervisorId() { return supervisorId; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public String getSlot() { return slot; }
    public UUID getPaathshaalaId() { return paathshaalaId; }

    public void setPaathshaalaId(UUID paathshaalaId) { this.paathshaalaId = paathshaalaId; }
    public void setSupervisorId(UUID supervisorId) { this.supervisorId = supervisorId; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setSlot(String slot) { this.slot = slot; }
}
