package com.novacore.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novacore.entity.ReferralTracking;

@Repository
public interface ReferralTrackingRepository extends JpaRepository<ReferralTracking, UUID> {
    Optional<ReferralTracking> findByReferredId(UUID referredId);
    List<ReferralTracking> findByReferrerIdOrderByCreatedAtDesc(UUID referrerId);
    long countByReferrerId(UUID referrerId);
    @Query("SELECT SUM(rt.totalEarnedFromReferral) FROM ReferralTracking rt WHERE rt.referrer.id = :id")
    BigDecimal totalEarnedByReferrer(@Param("id") UUID referrerId);
    @Query("SELECT COUNT(rt) FROM ReferralTracking rt WHERE rt.referrer.id = :id AND rt.miningBonusPaid = true")
    long countActiveMinersByReferrer(@Param("id") UUID referrerId);
}
