package com.pathshala.stub.repository;

import com.pathshala.stub.entity.SupervisorTimetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupervisorTimetableRepository extends JpaRepository<SupervisorTimetable, UUID> {
    List<SupervisorTimetable> findBySupervisorId(UUID supervisorId);
    Optional<SupervisorTimetable> findBySupervisorIdAndDayOfWeekAndSlot(UUID supervisorId, Integer dayOfWeek, String slot);
}
