package com.novacore.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public
class UserProfileResponse {
    private String id;
    private String userId;
    private String email;
    private String displayName;
    private String photoUrl;
    private String referralCode;
    private BigDecimal novaBalance;
    private BigDecimal totalEarned;
    private Integer totalClaims;
    private Integer referralCount;
    private BigDecimal miningSpeed;
    private Integer currentStreak;
    private List<String> roles;
    private String createdAt;
}
