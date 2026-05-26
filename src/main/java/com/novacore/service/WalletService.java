package com.novacore.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.novacore.dto.response.WalletResponse;
import com.novacore.entity.User;
import com.novacore.entity.WalletTransaction;
import com.novacore.repository.UserRepository;
import com.novacore.repository.WalletTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepo;
    private final WalletTransactionRepository txRepo;

    public WalletResponse getWallet(String email) {
        User u = userRepo.findByEmail(email).orElseThrow();
        return WalletResponse.builder()
            .novaBalance(u.getNovaBalance())
            .totalEarned(u.getTotalEarned())
            .usdValue(u.getNovaBalance().multiply(new BigDecimal("0.0003")))
            .build();
    }

    public Page<WalletTransaction> getTransactions(String email, int page) {
        User u = userRepo.findByEmail(email).orElseThrow();
        return txRepo.findByUserIdOrderByCreatedAtDesc(
            u.getId(), PageRequest.of(page, 20));
    }
}

