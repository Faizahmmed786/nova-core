package com.novacore.controller;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

import com.novacore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/leaderboard")
@RequiredArgsConstructor
class LeaderboardController {

    private final UserRepository userRepo;

    @GetMapping
    public ResponseEntity<?> getLeaderboard() {
        var top = userRepo.findTopEarners(
            org.springframework.data.domain.PageRequest.of(0, 20)).getContent();
        return ResponseEntity.ok(top.stream().map(u -> Map.of(
            "userId",       u.getUserId(),
            "displayName",  u.getDisplayName(),
            "photoUrl",     u.getPhotoUrl() != null ? u.getPhotoUrl() : "",
            "totalEarned",  u.getTotalEarned(),
            "totalClaims",  u.getTotalClaims()
        )).collect(Collectors.toList()));
    }
}
