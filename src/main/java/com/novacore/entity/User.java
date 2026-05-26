package com.novacore.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "users", indexes = {
	    @Index(name = "idx_user_email", columnList = "email", unique = true),
	    @Index(name = "idx_user_userId", columnList = "user_id", unique = true),
	    @Index(name = "idx_user_referral_code", columnList = "referral_code", unique = true)
	})
	@EntityListeners(AuditingEntityListener.class)
	@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
	public class User {

	    @Id @GeneratedValue(strategy = GenerationType.UUID)
	    private UUID id;

	    @Column(nullable = false, unique = true)
	    private String email;

	    @Column(nullable = false)
	    private String displayName;

	    @Column
	    private String photoUrl;

	    /** faiz@48372 */
	    @Column(nullable = false, unique = true, name = "user_id")
	    private String userId;

	    /** 5-digit unique referral code */
	    @Column(nullable = false, unique = true, name = "referral_code", length = 10)
	    private String referralCode;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "referred_by_id")
	    private User referredBy;

	    @Column(nullable = false, precision = 18, scale = 6)
	    @Builder.Default
	    private BigDecimal novaBalance = BigDecimal.ZERO;

	    @Column(nullable = false, precision = 18, scale = 6)
	    @Builder.Default
	    private BigDecimal totalEarned = BigDecimal.ZERO;

	    @Column(nullable = false)
	    @Builder.Default
	    private Integer totalClaims = 0;

	    @Column(nullable = false)
	    @Builder.Default
	    private Integer referralCount = 0;

	    @Column(nullable = false, precision = 8, scale = 4)
	    @Builder.Default
	    private BigDecimal miningSpeed = new BigDecimal("0.1");

	    @Column(nullable = false)
	    @Builder.Default
	    private Integer currentStreak = 0;

	    @Column
	    private LocalDateTime lastClaimAt;

	    @Column(nullable = false)
	    @Builder.Default
	    private Boolean isActive = true;

	    @Column(nullable = false)
	    @Builder.Default
	    private Boolean isBanned = false;

	    @Column(nullable = false)
	    @Builder.Default
	    private String role = "ROLE_USER";

	    @CreatedDate
	    @Column(nullable = false, updatable = false)
	    private LocalDateTime createdAt;
	}

