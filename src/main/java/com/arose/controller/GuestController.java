package com.arose.controller;

import com.arose.entity.GuestSession;
import com.arose.service.GuestTrialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/guest")
public class GuestController {

    private final GuestTrialService guestTrialService;

    public GuestController(GuestTrialService guestTrialService) {
        this.guestTrialService = guestTrialService;
    }

    @PostMapping("/session")
    public ResponseEntity<?> createSession() {

        GuestSession session =
                guestTrialService.createGuestSession();

        return ResponseEntity.ok(
                Map.of(
                        "guestToken", session.getGuestToken(),
                        "remainingTrials", 2
                )
        );
    }

    @GetMapping("/trials")
    public ResponseEntity<?> getRemainingTrials(
            @RequestHeader("X-Guest-Token") String guestToken
    ) {

        int remaining =
                guestTrialService.getRemainingTrials(guestToken);

        return ResponseEntity.ok(
                Map.of(
                        "remainingTrials", remaining
                )
        );
    }

    @PostMapping("/trial")
    public ResponseEntity<?> consumeTrial(
            @RequestHeader("X-Guest-Token") String guestToken
    ) {

        GuestSession session =
                guestTrialService.consumeTrial(guestToken);

        return ResponseEntity.ok(
                Map.of(
                        "remainingTrials",
                        2 - session.getTrialCount()
                )
        );
    }
}