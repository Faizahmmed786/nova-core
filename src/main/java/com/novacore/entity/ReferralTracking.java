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

@Entity @Table(name = "referral_tracking", indexes = {
	    @Index(name = "idx_rt_referrer", columnList = "referrer_id"),
	    @Index(name = "idx_rt_referred", columnList = "referred_id", unique = true)
	})
	@EntityListeners(AuditingEntityListener.class)
	@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
	public class ReferralTracking {

	    @Id @GeneratedValue(strategy = GenerationType.UUID)
	    private UUID id;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "referrer_id", nullable = false)
	    private User referrer;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "referred_id", nullable = false, unique = true)
	    private User referred;

	    @Column(nullable = false)
	    @Builder.Default
	    private Boolean registerBonusPaid = false;

	    @Column(nullable = false)
	    @Builder.Default
	    private Boolean miningBonusPaid = false;

	    @Column(nullable = false)
	    @Builder.Default
	    private Integer totalPerClaimBonus = 0;

	    @Column(nullable = false, precision = 18, scale = 6)
	    @Builder.Default
	    private BigDecimal totalEarnedFromReferral = BigDecimal.ZERO;

	    @CreatedDate
	    @Column(nullable = false, updatable = false)
	    private LocalDateTime createdAt;
	}

