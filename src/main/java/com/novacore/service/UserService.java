package com.novacore.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.novacore.dto.response.DashboardResponse;
import com.novacore.dto.response.UserProfileResponse;
import com.novacore.entity.User;
import com.novacore.exception.NovaCoreException;
import com.novacore.repository.MiningHistoryRepository;
import com.novacore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final MiningHistoryRepository miningRepo;

    public UserProfileResponse getProfile(String email) {
        User u = userRepo.findByEmail(email).orElseThrow(() -> new NovaCoreException("User not found"));
        return toProfile(u);
    }

    public DashboardResponse getDashboard(String email) {
        User u = userRepo.findByEmail(email).orElseThrow(() -> new NovaCoreException("User not found"));
        return DashboardResponse.builder()
            .novaBalance(u.getNovaBalance())
            .totalEarned(u.getTotalEarned())
            .totalClaims(u.getTotalClaims())
            .referralCount(u.getReferralCount())
            .miningSpeed(u.getMiningSpeed())
            .currentStreak(u.getCurrentStreak())
            .userId(u.getUserId())
            .referralCode(u.getReferralCode())
            .build();
    }

    public UserProfileResponse toProfile(User u) {
        return UserProfileResponse.builder()
            .id(u.getId().toString())
            .userId(u.getUserId())
            .email(u.getEmail())
            .displayName(u.getDisplayName())
            .photoUrl(u.getPhotoUrl())
            .referralCode(u.getReferralCode())
            .novaBalance(u.getNovaBalance())
            .totalEarned(u.getTotalEarned())
            .totalClaims(u.getTotalClaims())
            .referralCount(u.getReferralCount())
            .miningSpeed(u.getMiningSpeed())
            .currentStreak(u.getCurrentStreak())
            .roles(List.of(u.getRole()))
            .createdAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null)
            .build();
    }
}
