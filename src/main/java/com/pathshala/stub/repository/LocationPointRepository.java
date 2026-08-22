package com.pathshala.stub.repository;

import com.pathshala.stub.entity.LocationPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LocationPointRepository extends JpaRepository<LocationPoint, Long> {
    List<LocationPoint> findByUserIdOrderByCapturedAtAsc(UUID userId);
}
