package com.novacore.entity;

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

@Entity @Table(name = "refresh_tokens", indexes = {
	    @Index(name = "idx_rft_token", columnList = "token", unique = true),
	    @Index(name = "idx_rft_user", columnList = "user_id")
	})
	@EntityListeners(AuditingEntityListener.class)
	@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
	public class RefreshToken {

	    @Id @GeneratedValue(strategy = GenerationType.UUID)
	    private UUID id;

	    @Column(nullable = false, unique = true, length = 512)
	    private String token;

	    @ManyToOne(fetch = FetchType.EAGER)
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;

	    @Column(nullable = false)
	    private LocalDateTime expiresAt;

	    @Column(nullable = false)
	    @Builder.Default
	    private Boolean revoked = false;

	    @CreatedDate
	    @Column(nullable = false, updatable = false)
	    private LocalDateTime createdAt;
	}
