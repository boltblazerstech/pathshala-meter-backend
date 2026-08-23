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
    @Query(value = """
            SELECT * FROM location_pings lp
            WHERE (cast(:userId as text)::uuid IS NULL OR lp.user_id = cast(:userId as text)::uuid)
              AND (cast(:from as text)::timestamp IS NULL OR lp.captured_at >= cast(:from as text)::timestamp)
              AND (cast(:to as text)::timestamp IS NULL OR lp.captured_at <= cast(:to as text)::timestamp)
            ORDER BY lp.captured_at ASC
            """, nativeQuery = true)
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

    @Query(value = """
            SELECT DISTINCT ON (user_id) *
            FROM location_pings
            WHERE user_id IN :userIds
            ORDER BY user_id, captured_at DESC
            """, nativeQuery = true)
    List<LocationPoint> findLatestPingsForUsers(@Param("userIds") java.util.Collection<UUID> userIds);
}
