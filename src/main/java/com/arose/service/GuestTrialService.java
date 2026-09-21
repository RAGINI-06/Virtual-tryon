package com.arose.service;

import com.arose.entity.GuestSession;
import com.arose.repository.GuestSessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GuestTrialService {

    private static final int MAX_FREE_TRIALS = 2;

    private final GuestSessionRepository guestSessionRepository;

    public GuestTrialService(
            GuestSessionRepository guestSessionRepository
    ) {
        this.guestSessionRepository = guestSessionRepository;
    }

    public GuestSession createGuestSession() {

        GuestSession guestSession = GuestSession.builder()
                .guestToken(UUID.randomUUID().toString())
                .trialCount(0)
                .build();

        return guestSessionRepository.save(guestSession);
    }

    public boolean canUseTrial(String guestToken) {

        GuestSession guestSession = guestSessionRepository
                .findByGuestToken(guestToken)
                .orElseThrow(() ->
                        new RuntimeException("Invalid guest session")
                );

        return guestSession.getTrialCount() < MAX_FREE_TRIALS;
    }

    public GuestSession consumeTrial(String guestToken) {

        GuestSession guestSession = guestSessionRepository
                .findByGuestToken(guestToken)
                .orElseThrow(() ->
                        new RuntimeException("Invalid guest session")
                );

        if (guestSession.getTrialCount() >= MAX_FREE_TRIALS) {
            throw new RuntimeException(
                    "Free trials exhausted. Please login or register to continue."
            );
        }

        guestSession.setTrialCount(
                guestSession.getTrialCount() + 1
        );

        return guestSessionRepository.save(guestSession);
    }

    public int getRemainingTrials(String guestToken) {

        GuestSession guestSession = guestSessionRepository
                .findByGuestToken(guestToken)
                .orElseThrow(() ->
                        new RuntimeException("Invalid guest session")
                );

        return Math.max(
                0,
                MAX_FREE_TRIALS - guestSession.getTrialCount()
        );
    }
}