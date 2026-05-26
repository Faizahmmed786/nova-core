package com.novacore.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.novacore.dto.request.GoogleLoginRequest;
import com.novacore.dto.response.AuthResponse;
import com.novacore.entity.RefreshToken;
import com.novacore.entity.User;
import com.novacore.entity.ReferralTracking;
import com.novacore.entity.WalletTransaction;
import com.novacore.exception.NovaCoreException;
import com.novacore.repository.*;
import com.novacore.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final ReferralTrackingRepository referralRepo;
    private final WalletTransactionRepository walletRepo;
    private final JwtUtils jwtUtils;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiry;

    @Value("${app.referral.register-bonus}")
    private BigDecimal registerBonus;

    // ── GOOGLE LOGIN / REGISTER ───────────────────────────────────────────────
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest req) {
        // 1. Verify Google ID token
        GoogleIdToken.Payload payload = verifyGoogleToken(req.getIdToken());
        String email = payload.getEmail();
        String displayName = (String) payload.get("name");
        String photoUrl = (String) payload.get("picture");

        // 2. Find or create user
        User user = userRepo.findByEmail(email).orElseGet(() ->
            createNewUser(email, displayName, photoUrl, req.getReferralCode())
        );

        // Update profile photo if changed
        if (photoUrl != null && !photoUrl.equals(user.getPhotoUrl())) {
            user.setPhotoUrl(photoUrl);
            userRepo.save(user);
        }

        return buildTokens(user);
    }

    // ── REFRESH TOKEN ─────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse refresh(String refreshTokenStr) {
        RefreshToken rt = refreshRepo.findByToken(refreshTokenStr)
            .orElseThrow(() -> new NovaCoreException("Invalid refresh token"));

        if (rt.getRevoked() || rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new NovaCoreException("Refresh token expired");
        }

        rt.setRevoked(true);
        refreshRepo.save(rt);
        return buildTokens(rt.getUser());
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────
    @Transactional
    public void logout(String refreshTokenStr) {
        refreshRepo.findByToken(refreshTokenStr).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshRepo.save(rt);
        });
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────
    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) throw new NovaCoreException("Invalid Google token");
            return token.getPayload();
        } catch (NovaCoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new NovaCoreException("Google authentication failed");
        }
    }

    @Transactional
    protected User createNewUser(String email, String displayName, String photoUrl, String referralCode) {
        // Extract username from email (before @)
        String usernameBase = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");

        // Generate unique userId: username@XXXXX
        String userId = generateUniqueUserId(usernameBase);

        // Generate unique 5-digit referral code
        String newReferralCode = generateUniqueReferralCode();

        User.UserBuilder builder = User.builder()
            .email(email)
            .displayName(displayName != null ? displayName : usernameBase)
            .photoUrl(photoUrl)
            .userId(userId)
            .referralCode(newReferralCode);

        // Handle referral
        if (referralCode != null && !referralCode.isBlank()) {
            userRepo.findAll().stream()
                .filter(u -> newReferralCode.equals(u.getReferralCode()) == false
                    && referralCode.equals(u.getReferralCode()))
                .findFirst()
                .ifPresent(referrer -> {
                    builder.referredBy(referrer);
                });
        }

        User saved = userRepo.save(builder.build());

        // Pay referral register bonus
        if (saved.getReferredBy() != null) {
            User referrer = saved.getReferredBy();
            creditUser(referrer, registerBonus, WalletTransaction.TxType.REFERRAL_REGISTER,
                "Referral register bonus: " + saved.getDisplayName());
            referrer.setReferralCount(referrer.getReferralCount() + 1);
            userRepo.save(referrer);

            referralRepo.save(ReferralTracking.builder()
                .referrer(referrer)
                .referred(saved)
                .registerBonusPaid(true)
                .build());
        }

        log.info("New user created: {} ({})", saved.getDisplayName(), saved.getUserId());
        return saved;
    }

    private String generateUniqueUserId(String base) {
        String candidate;
        do {
            int suffix = 10000 + new Random().nextInt(90000);
            candidate = base + "@" + suffix;
        } while (userRepo.existsByUserId(candidate));
        return candidate;
    }

    private String generateUniqueReferralCode() {
        String code;
        do {
            code = String.valueOf(10000 + new Random().nextInt(90000));
        } while (userRepo.existsByReferralCode(code));
        return code;
    }

    private AuthResponse buildTokens(User user) {
        Map<String, Object> claims = Map.of(
            "userId", user.getUserId(),
            "role", user.getRole()
        );
        String accessToken = jwtUtils.generateAccess(user.getEmail(), claims);
        String refreshToken = jwtUtils.generateRefresh(user.getEmail());

        // Revoke old refresh tokens
        refreshRepo.revokeAllByUser(user.getId());

        refreshRepo.save(RefreshToken.builder()
            .token(refreshToken)
            .user(user)
            .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiry / 1000))
            .build());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtUtils.getAccessExpiry() / 1000)
            .build();
    }

    @Transactional
    public void creditUser(User user, BigDecimal amount, WalletTransaction.TxType type, String description) {
        user.setNovaBalance(user.getNovaBalance().add(amount));
        user.setTotalEarned(user.getTotalEarned().add(amount));
        userRepo.save(user);

        walletRepo.save(WalletTransaction.builder()
            .user(user)
            .type(type)
            .amount(amount)
            .balanceAfter(user.getNovaBalance())
            .description(description)
            .build());
    }
}
