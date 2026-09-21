package com.arose.controller;

import com.arose.dto.consent.ConsentRequest;
import com.arose.dto.consent.ConsentResponse;
import com.arose.service.ConsentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consent")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping
    public ResponseEntity<ConsentResponse> giveConsent(
            Authentication authentication,
            @Valid @RequestBody ConsentRequest request
    ) {

        String userId = authentication.getName();

        ConsentResponse response =
                consentService.giveConsent(userId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ConsentResponse> getConsent(
            Authentication authentication
    ) {

        String userId = authentication.getName();

        ConsentResponse response =
                consentService.getConsent(userId);

        return ResponseEntity.ok(response);
    }
}