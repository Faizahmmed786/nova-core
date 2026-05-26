package com.novacore.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.novacore.dto.response.AdminStatsResponse;
import com.novacore.entity.User;
import com.novacore.repository.MiningHistoryRepository;
import com.novacore.repository.ReferralTrackingRepository;
import com.novacore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepo;
    private final MiningHistoryRepository miningRepo;
    private final ReferralTrackingRepository referralRepo;

    public AdminStatsResponse getStats() {
        long totalUsers = userRepo.count();
        long activeUsers = userRepo.countByIsActiveTrue();
        BigDecimal totalCirculating = userRepo.sumAllBalances();
        BigDecimal totalEverEarned = userRepo.sumAllEarned();
        BigDecimal totalDistributed = miningRepo.totalPointsDistributed();

        return AdminStatsResponse.builder()
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .totalNovaCirculating(totalCirculating != null ? totalCirculating : BigDecimal.ZERO)
            .totalEverEarned(totalEverEarned != null ? totalEverEarned : BigDecimal.ZERO)
            .totalPointsDistributed(totalDistributed != null ? totalDistributed : BigDecimal.ZERO)
            .build();
    }

    public Page<User> getUsers(int page, String search) {
        var pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        return (search != null && !search.isBlank())
            ? userRepo.searchUsers(search, pageable)
            : userRepo.findAll(pageable);
    }

    public void banUser(UUID userId, boolean ban) {
        User u = userRepo.findById(userId).orElseThrow();
        u.setIsBanned(ban);
        userRepo.save(u);
    }
}

