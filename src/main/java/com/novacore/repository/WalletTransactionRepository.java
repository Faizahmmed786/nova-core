package com.novacore.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novacore.entity.WalletTransaction;

@Repository
public
interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    Page<WalletTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    @Query("SELECT SUM(w.amount) FROM WalletTransaction w WHERE w.user.id = :uid AND w.type = 'MINING_CLAIM'")
    BigDecimal totalMiningEarned(@Param("uid") UUID userId);
}
