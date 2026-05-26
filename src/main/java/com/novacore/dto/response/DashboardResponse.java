package com.novacore.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public
class DashboardResponse {
    private BigDecimal novaBalance;
    private BigDecimal totalEarned;
    private Integer totalClaims;
    private Integer referralCount;
    private BigDecimal miningSpeed;
    private Integer currentStreak;
    private String userId;
    private String referralCode;
}
