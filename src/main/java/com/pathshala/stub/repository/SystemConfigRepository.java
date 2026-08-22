package com.pathshala.stub.repository;

import com.pathshala.stub.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
    // findById(key) inherited from JpaRepository — used to read/write config values
}
