package com.pathshala.stub.repository;

import com.pathshala.stub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    /** Used at field-app login: must match phone AND be active. */
    Optional<User> findByPhoneNumberAndActiveTrue(String phoneNumber);
}
