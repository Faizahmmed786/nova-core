package com.novacore.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novacore.dto.response.WalletResponse;
import com.novacore.entity.WalletTransaction;
import com.novacore.service.WalletService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletResponse> getWallet(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(walletService.getWallet(ud.getUsername()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page) {
        Page<WalletTransaction> txPage = walletService.getTransactions(ud.getUsername(), page);
        var content = txPage.getContent().stream().map(tx -> Map.of(
            "id",          tx.getId(),
            "type",        tx.getType().name(),
            "amount",      tx.getAmount(),
            "balanceAfter",tx.getBalanceAfter(),
            "description", tx.getDescription() != null ? tx.getDescription() : "",
            "date",        tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : ""
        )).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "content",       content,
            "totalElements", txPage.getTotalElements(),
            "totalPages",    txPage.getTotalPages(),
            "page",          page
        ));
    }
}
