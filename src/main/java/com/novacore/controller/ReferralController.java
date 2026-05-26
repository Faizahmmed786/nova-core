package com.novacore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.novacore.dto.response.ReferralInfoResponse;
import com.novacore.service.ReferralService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @GetMapping
    public ResponseEntity<ReferralInfoResponse> getReferralInfo(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(referralService.getReferralInfo(ud.getUsername()));
    }
}

