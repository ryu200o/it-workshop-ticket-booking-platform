package com.example.itworkshopticketbookingplatform.workshop.internal.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkshopJpaRepository extends JpaRepository<WorkshopJpaEntity, UUID> {

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM WorkshopJpaEntity w " +
           "WHERE w.roomId = :roomId " +
           "AND w.startTime < :endTime " +
           "AND w.endTime > :startTime " +
           "AND w.state IN ('PUBLISHED', 'IN_PROGRESS')")
    boolean existsByRoomIdAndTimeRange(@Param("roomId") UUID roomId,
                                        @Param("startTime") Instant startTime,
                                        @Param("endTime") Instant endTime);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM WorkshopJpaEntity w " +
           "WHERE w.roomId = :roomId " +
           "AND w.startTime < :endTime " +
           "AND w.endTime > :startTime " +
           "AND w.state IN ('PUBLISHED', 'IN_PROGRESS') " +
           "AND w.id != :excludeWorkshopId")
    boolean existsByRoomIdAndTimeRangeExcluding(@Param("roomId") UUID roomId,
                                                  @Param("startTime") Instant startTime,
                                                  @Param("endTime") Instant endTime,
                                                  @Param("excludeWorkshopId") UUID excludeWorkshopId);
}