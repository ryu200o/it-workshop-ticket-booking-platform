package com.example.itworkshopticketbookingplatform.workshop.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

interface WorkshopRepository extends JpaRepository<Workshop, UUID> {

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Workshop w " +
           "WHERE w.roomId = :roomId " +
           "AND w.startTime < :endTime " +
           "AND w.endTime > :startTime " +
           "AND w.state IN ('PUBLISHED', 'IN_PROGRESS')")
    boolean existsByRoomIdAndTimeRange(@Param("roomId") UUID roomId,
                                        @Param("startTime") Instant startTime,
                                        @Param("endTime") Instant endTime);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Workshop w " +
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