package com.novacore.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novacore.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId);
    boolean existsByEmail(String email);
    boolean existsByReferralCode(String code);
    boolean existsByUserId(String userId);
    long countByIsActiveTrue();
    long countByIsBannedTrue();
    @Query("SELECT SUM(u.novaBalance) FROM User u")
    BigDecimal sumAllBalances();
    @Query("SELECT SUM(u.totalEarned) FROM User u")
    BigDecimal sumAllEarned();
    @Query("SELECT u FROM User u ORDER BY u.totalEarned DESC")
    Page<User> findTopEarners(Pageable pageable);
    @Query("SELECT u FROM User u ORDER BY u.referralCount DESC")
    Page<User> findTopReferrers(Pageable pageable);
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.displayName) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<User> searchUsers(@Param("q") String q, Pageable pageable);
}
