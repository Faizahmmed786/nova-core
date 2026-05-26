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

@Entity @Table(name = "mining_history", indexes = {
	    @Index(name = "idx_mh_user", columnList = "user_id"),
	    @Index(name = "idx_mh_slot", columnList = "slot_time"),
	    @Index(name = "idx_mh_user_slot", columnList = "user_id,slot_time", unique = true)
	})
	@EntityListeners(AuditingEntityListener.class)
	@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MiningHistory {

	    @Id @GeneratedValue(strategy = GenerationType.UUID)
	    private UUID id;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;

	    /** Normalised slot time e.g. 2024-05-23T06:00 */
	    @Column(nullable = false, name = "slot_time")
	    private LocalDateTime slotTime;

	    @Column(nullable = false, precision = 18, scale = 6)
	    private BigDecimal pointsEarned;

	    @Column
	    private String ipAddress;

	    @CreatedDate
	    @Column(nullable = false, updatable = false)
	    private LocalDateTime claimedAt;
	}

