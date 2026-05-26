package com.novacore.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.novacore.dto.response.ReferralInfoResponse;
import com.novacore.dto.response.ReferralUserDto;
import com.novacore.entity.ReferralTracking;
import com.novacore.entity.User;
import com.novacore.repository.ReferralTrackingRepository;
import com.novacore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final UserRepository userRepo;
    private final ReferralTrackingRepository trackingRepo;

    public ReferralInfoResponse getReferralInfo(String email) {
        User u = userRepo.findByEmail(email).orElseThrow();
        List<ReferralTracking> referrals = trackingRepo.findByReferrerIdOrderByCreatedAtDesc(u.getId());

        BigDecimal totalReferralEarned = trackingRepo.totalEarnedByReferrer(u.getId());
        long activeMinerCount = trackingRepo.countActiveMinersByReferrer(u.getId());

        List<ReferralUserDto> dtos = referrals.stream().map(rt -> ReferralUserDto.builder()
            .userId(rt.getReferred().getUserId())
            .displayName(rt.getReferred().getDisplayName())
            .photoUrl(rt.getReferred().getPhotoUrl())
            .hasMined(rt.getMiningBonusPaid())
            .pointsEarned(rt.getTotalEarnedFromReferral())
            .joinedAt(rt.getCreatedAt() != null ? rt.getCreatedAt().toString() : null)
            .build()
        ).collect(Collectors.toList());

        return ReferralInfoResponse.builder()
            .referralCode(u.getReferralCode())
            .totalReferrals((long) referrals.size())
            .totalReferralEarned(totalReferralEarned != null ? totalReferralEarned : BigDecimal.ZERO)
            .activeMinerCount(activeMinerCount)
            .currentStreak(u.getCurrentStreak())
            .referrals(dtos)
            .build();
    }
}
