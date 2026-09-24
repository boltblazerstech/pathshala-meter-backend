package com.pathshala.stub.repository;

import com.pathshala.stub.entity.SupervisorTimetableOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupervisorTimetableOverrideRepository extends JpaRepository<SupervisorTimetableOverride, UUID> {
    Optional<SupervisorTimetableOverride> findBySupervisorIdAndOverrideDateAndSlot(UUID supervisorId, LocalDate overrideDate, String slot);
    List<SupervisorTimetableOverride> findBySupervisorIdAndOverrideDateBetweenOrderByOverrideDateAscSlotAsc(UUID supervisorId, LocalDate from, LocalDate to);
}
