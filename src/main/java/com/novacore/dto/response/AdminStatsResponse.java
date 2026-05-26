package com.novacore.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminStatsResponse {
    private Long totalUsers;
    private Long activeUsers;
    private BigDecimal totalNovaCirculating;
    private BigDecimal totalEverEarned;
    private BigDecimal totalPointsDistributed;
}
