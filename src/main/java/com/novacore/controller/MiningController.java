package com.novacore.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.novacore.dto.response.ApiResponse;
import com.novacore.dto.response.MiningClaimResponse;
import com.novacore.dto.response.MiningStatusResponse;
import com.novacore.repository.UserRepository;
import com.novacore.service.MiningService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mining")
@RequiredArgsConstructor
public class MiningController {

    private final MiningService miningService;
    private final UserRepository userRepo;

    @GetMapping("/status")
    public ResponseEntity<MiningStatusResponse> getStatus(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = userRepo.findByEmail(ud.getUsername()).orElseThrow().getId();
        return ResponseEntity.ok(miningService.getStatus(userId));
    }

    @PostMapping("/claim")
    public ResponseEntity<MiningClaimResponse> claim(
            @AuthenticationPrincipal UserDetails ud,
            HttpServletRequest req) {
        UUID userId = userRepo.findByEmail(ud.getUsername()).orElseThrow().getId();
        String ip = req.getRemoteAddr();
        return ResponseEntity.ok(miningService.claim(userId, ip));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = userRepo.findByEmail(ud.getUsername()).orElseThrow().getId();
        return ResponseEntity.ok(ApiResponse.ok(miningService.getHistory(userId)));
    }
}
