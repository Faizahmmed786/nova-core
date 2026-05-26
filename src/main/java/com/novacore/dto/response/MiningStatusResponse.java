package com.novacore.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MiningStatusResponse {
    private boolean canClaim;
    private LocalDateTime currentSlotTime;
    private LocalDateTime nextClaimAt;
    private long secondsUntilClaim;
    private int todayProgress;
    private int totalSlots;
    private LocalDateTime lastClaimAt;
}
