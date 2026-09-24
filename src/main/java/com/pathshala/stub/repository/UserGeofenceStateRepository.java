package com.pathshala.stub.repository;

import com.pathshala.stub.entity.UserGeofenceState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserGeofenceStateRepository extends JpaRepository<UserGeofenceState, UUID> {
}
