package com.novacore.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public
class MiningClaimResponse {
    private BigDecimal pointsEarned;
    private BigDecimal newBalance;
    private LocalDateTime slotTime;
    private LocalDateTime nextClaimAt;
    private int totalClaims;
}
