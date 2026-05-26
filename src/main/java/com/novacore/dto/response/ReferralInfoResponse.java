package com.novacore.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public
class ReferralInfoResponse {
    private String referralCode;
    private Long totalReferrals;
    private BigDecimal totalReferralEarned;
    private Long activeMinerCount;
    private Integer currentStreak;
    private List<ReferralUserDto> referrals;
}
