package com.novacore.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public
class WalletResponse {
    private BigDecimal novaBalance;
    private BigDecimal totalEarned;
    private BigDecimal usdValue;
}
