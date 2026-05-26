package com.novacore.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novacore.entity.MiningHistory;



@Repository
public
interface MiningHistoryRepository extends JpaRepository<MiningHistory, UUID> {
    boolean existsByUserIdAndSlotTime(UUID userId, LocalDateTime slotTime);
    List<MiningHistory> findByUserIdOrderByClaimedAtDesc(UUID userId);
    long countByUserIdAndSlotTimeBetween(UUID userId, LocalDateTime start, LocalDateTime end);
    @Query("SELECT COUNT(m) FROM MiningHistory m WHERE m.slotTime >= :since")
    long countClaimsSince(@Param("since") LocalDateTime since);
    @Query("SELECT SUM(m.pointsEarned) FROM MiningHistory m")
    BigDecimal totalPointsDistributed();
}
