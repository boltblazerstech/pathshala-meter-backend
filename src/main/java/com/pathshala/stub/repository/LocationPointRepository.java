package com.pathshala.stub.repository;

import com.pathshala.stub.entity.LocationPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LocationPointRepository extends JpaRepository<LocationPoint, Long> {

    /**
     * GET /api/locations — all pings for a user, optionally bounded by a time range.
     * Both from and to are inclusive; pass null to leave that bound open.
     */
    @Query("""
            SELECT lp FROM LocationPoint lp
            WHERE lp.userId = :userId
              AND (:from IS NULL OR lp.capturedAt >= :from)
              AND (:to   IS NULL OR lp.capturedAt <= :to)
            ORDER BY lp.capturedAt ASC
            """)
    List<LocationPoint> findByUserIdAndRange(
            @Param("userId") UUID userId,
            @Param("from")   Instant from,
            @Param("to")     Instant to);

    /** Admin: Get the single most recent ping for every user. */
    @Query(value = """
            SELECT DISTINCT ON (user_id) *
            FROM location_pings
            ORDER BY user_id, captured_at DESC
            """, nativeQuery = true)
    List<LocationPoint> findLatestPingsPerUser();
}
