package com.pathshala.stub.repository;

import com.pathshala.stub.entity.Paathshaala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaathshalaRepository extends JpaRepository<Paathshaala, UUID> {
    // All queries beyond standard JPA are delegated to UserRepository
    // (e.g. countByAssignedPaathshalaId) to decide whether delete is safe.
}
