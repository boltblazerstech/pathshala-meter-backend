package com.pathshala.stub.repository;

import com.pathshala.stub.entity.TrackingWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackingWindowRepository extends JpaRepository<TrackingWindow, UUID> {

    /**
     * Field-app: the single most-recent active window for one user as of a given date.
     * "Most-recent" means the highest effective_from_date that is still <= :date.
     */
    @Query(value = """
            SELECT * FROM tracking_windows
            WHERE user_id = :userId
              AND effective_from_date <= :date
              AND is_active = true
            ORDER BY effective_from_date DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<TrackingWindow> findCurrentForUser(
            @Param("userId") UUID userId,
            @Param("date")   LocalDate date);

    /**
     * Admin overview: every user's most-recent active window as of :date.
     * Uses Postgres DISTINCT ON to pick one row per user in a single scan.
     */
    @Query(value = """
            SELECT DISTINCT ON (user_id) *
            FROM tracking_windows
            WHERE effective_from_date <= :date
              AND is_active = true
            ORDER BY user_id, effective_from_date DESC
            """, nativeQuery = true)
    List<TrackingWindow> findAllEffectiveAsOf(@Param("date") LocalDate date);
}
