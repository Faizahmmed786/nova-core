package com.novacore.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public
class ReferralUserDto {
    private String userId;
    private String displayName;
    private String photoUrl;
    private Boolean hasMined;
    private BigDecimal pointsEarned;
    private String joinedAt;
}
