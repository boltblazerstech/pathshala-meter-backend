package com.pathshala.stub.repository;

import com.pathshala.stub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** Used at field-app login: must match phone AND be active. */
    Optional<User> findByPhoneNumberAndActiveTrue(String phoneNumber);

    /** Paginated listing of a single role (supervisor or teacher) for admin. */
    Page<User> findByRoleOrderByCreatedAtDesc(String role, Pageable pageable);

    /** Guards paathshaala deletes — block if any user (active or not) is still assigned. */
    long countByAssignedPaathshalaId(UUID paathshalaId);
}
