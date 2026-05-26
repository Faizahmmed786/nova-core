package com.novacore.service;

import com.novacore.dto.response.MiningClaimResponse;
import com.novacore.dto.response.MiningStatusResponse;
import com.novacore.entity.*;
import com.novacore.exception.NovaCoreException;
import com.novacore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MiningService {

    private final UserRepository userRepo;
    private final MiningHistoryRepository miningRepo;
    private final ReferralTrackingRepository referralRepo;
    private final AuthService authService;

    @Value("${app.mining.points-per-claim:40}")
    private BigDecimal pointsPerClaim;

    @Value("${app.referral.per-claim-bonus:2}")
    private BigDecimal perClaimBonus;

    @Value("${app.referral.mining-bonus:100}")
    private BigDecimal miningBonus;

    // Slot hours: 06, 10, 14, 18, 22, 02
    private static final int[] SLOT_HOURS = {6, 10, 14, 18, 22, 2};

    // ── GET MINING STATUS ─────────────────────────────────────────────────────
    public MiningStatusResponse getStatus(UUID userId) {
        User user = userRepo.findById(userId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime currentSlot = getCurrentSlot(now);
        LocalDateTime nextSlot = getNextSlot(now);

        boolean canClaim = !miningRepo.existsByUserIdAndSlotTime(userId, currentSlot);
        long secondsUntilNext = canClaim ? 0 : ChronoUnit.SECONDS.between(now, nextSlot);

        int todayProgress = countTodayProgress(userId, now);

        return MiningStatusResponse.builder()
            .canClaim(canClaim)
            .currentSlotTime(currentSlot)
            .nextClaimAt(canClaim ? null : nextSlot)
            .secondsUntilClaim(Math.max(0, secondsUntilNext))
            .todayProgress(todayProgress)
            .totalSlots(6)
            .lastClaimAt(user.getLastClaimAt())
            .build();
    }

    // ── CLAIM MINING ──────────────────────────────────────────────────────────
    @Transactional
    public MiningClaimResponse claim(UUID userId, String ipAddress) {
        User user = userRepo.findById(userId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentSlot = getCurrentSlot(now);

        // Anti-cheat: already claimed this slot
        if (miningRepo.existsByUserIdAndSlotTime(userId, currentSlot)) {
            throw new NovaCoreException("Already claimed for this slot. Come back at " + getNextSlot(now));
        }

        // Credit user
        authService.creditUser(user, pointsPerClaim,
            WalletTransaction.TxType.MINING_CLAIM,
            "Mining claim — slot " + currentSlot);

        // Record history
        miningRepo.save(MiningHistory.builder()
            .user(user)
            .slotTime(currentSlot)
            .pointsEarned(pointsPerClaim)
            .ipAddress(ipAddress)
            .build());

        // Update user claim stats
        user.setTotalClaims(user.getTotalClaims() + 1);
        user.setLastClaimAt(now);
        updateStreak(user, now);
        userRepo.save(user);

        // Pay referral bonuses (async-safe within TX)
        payReferralBonuses(user);

        log.info("Mining claimed by {} at slot {}, earned {} NC", user.getUserId(), currentSlot, pointsPerClaim);

        return MiningClaimResponse.builder()
            .pointsEarned(pointsPerClaim)
            .newBalance(user.getNovaBalance())
            .slotTime(currentSlot)
            .nextClaimAt(getNextSlot(now))
            .totalClaims(user.getTotalClaims())
            .build();
    }

    // ── GET HISTORY ───────────────────────────────────────────────────────────
    public List<MiningHistory> getHistory(UUID userId) {
        return miningRepo.findByUserIdOrderByClaimedAtDesc(userId);
    }

    // ── SLOT LOGIC ────────────────────────────────────────────────────────────
    /**
     * Returns the most recent slot start time that has passed.
     * Slots: 06:00, 10:00, 14:00, 18:00, 22:00, 02:00 (next day)
     */
    public LocalDateTime getCurrentSlot(LocalDateTime now) {
        int hour = now.getHour();

        // Find the most recent slot hour that is <= current hour
        int slotHour = -1;
        for (int h : new int[]{2, 6, 10, 14, 18, 22}) {
            if (hour >= h) slotHour = h;
        }

        if (slotHour == -1) {
            // We are between midnight and 02:00, so last slot was 22:00 yesterday
            return now.toLocalDate().minusDays(1).atTime(22, 0).truncatedTo(ChronoUnit.MINUTES);
        }
        return now.toLocalDate().atTime(slotHour, 0).truncatedTo(ChronoUnit.MINUTES);
    }

    public LocalDateTime getNextSlot(LocalDateTime now) {
        int hour = now.getHour();
        int[] slots = {2, 6, 10, 14, 18, 22};

        for (int h : slots) {
            if (h > hour) {
                return now.toLocalDate().atTime(h, 0);
            }
        }
        // After 22:00 → next is 02:00 next day
        return now.toLocalDate().plusDays(1).atTime(2, 0);
    }

    private int countTodayProgress(UUID userId, LocalDateTime now) {
        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        return (int) miningRepo.countByUserIdAndSlotTimeBetween(userId, dayStart, dayEnd);
    }

    // ── STREAK ────────────────────────────────────────────────────────────────
    private void updateStreak(User user, LocalDateTime now) {
        if (user.getLastClaimAt() == null) {
            user.setCurrentStreak(1);
            return;
        }
        long daysSinceLast = ChronoUnit.DAYS.between(
            user.getLastClaimAt().toLocalDate(), now.toLocalDate());

        if (daysSinceLast == 0) {
            // Same day, streak unchanged
        } else if (daysSinceLast == 1) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        } else {
            user.setCurrentStreak(1); // broken
        }
    }

    // ── REFERRAL BONUSES ──────────────────────────────────────────────────────
    @Transactional
    protected void payReferralBonuses(User claimingUser) {
        referralRepo.findByReferredId(claimingUser.getId()).ifPresent(rt -> {
            User referrer = rt.getReferrer();

            // First mining bonus (once)
            if (!rt.getMiningBonusPaid()) {
                authService.creditUser(referrer, miningBonus,
                    WalletTransaction.TxType.REFERRAL_MINING,
                    "Referral first mining: " + claimingUser.getDisplayName());
                rt.setMiningBonusPaid(true);
            }

            // Per-claim bonus (+2 NC every time)
            authService.creditUser(referrer, perClaimBonus,
                WalletTransaction.TxType.REFERRAL_BONUS,
                "Per-claim bonus: " + claimingUser.getDisplayName());

            rt.setTotalPerClaimBonus(rt.getTotalPerClaimBonus() + 1);
            rt.setTotalEarnedFromReferral(rt.getTotalEarnedFromReferral().add(perClaimBonus));
            referralRepo.save(rt);
        });
    }
}
