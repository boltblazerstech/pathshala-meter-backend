package com.pathshala.stub.repository;

import com.pathshala.stub.entity.GeofenceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface GeofenceEventRepository extends JpaRepository<GeofenceEvent, UUID> {
    List<GeofenceEvent> findByUserIdAndEventDateOrderByEventTimeAsc(UUID userId, LocalDate eventDate);
}
