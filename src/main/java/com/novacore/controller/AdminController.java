package com.novacore.controller;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novacore.dto.response.AdminStatsResponse;
import com.novacore.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String search) {
        var p = adminService.getUsers(page, search);
        return ResponseEntity.ok(Map.of(
            "content",       p.getContent().stream().map(u -> Map.of(
                "id",          u.getId(),
                "userId",      u.getUserId(),
                "displayName", u.getDisplayName(),
                "email",       u.getEmail(),
                "novaBalance", u.getNovaBalance(),
                "totalClaims", u.getTotalClaims(),
                "isBanned",    u.getIsBanned(),
                "createdAt",   u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
            )).collect(Collectors.toList()),
            "totalElements", p.getTotalElements(),
            "totalPages",    p.getTotalPages()
        ));
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<?> banUser(@PathVariable UUID userId) {
        adminService.banUser(userId, true);
        return ResponseEntity.ok(Map.of("message", "User banned"));
    }

    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable UUID userId) {
        adminService.banUser(userId, false);
        return ResponseEntity.ok(Map.of("message", "User unbanned"));
    }
}
