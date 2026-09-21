package com.arose.service;

import com.arose.dto.consent.ConsentRequest;
import com.arose.dto.consent.ConsentResponse;
import com.arose.entity.Consent;
import com.arose.entity.User;
import com.arose.repository.ConsentRepository;
import com.arose.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ConsentService {

    private static final String CURRENT_CONSENT_VERSION = "v1";

    private final ConsentRepository consentRepository;
    private final UserRepository userRepository;

    public ConsentService(
            ConsentRepository consentRepository,
            UserRepository userRepository
    ) {
        this.consentRepository = consentRepository;
        this.userRepository = userRepository;
    }

    public ConsentResponse giveConsent(
            String userId,
            ConsentRequest request
    ) {

        if (!request.isConsentGiven()) {
            throw new IllegalArgumentException("Consent is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Consent consent = consentRepository
                .findByUserId(userId)
                .orElse(
                        Consent.builder()
                                .user(user)
                                .build()
                );

        consent.setConsentVersion(CURRENT_CONSENT_VERSION);

        Consent savedConsent = consentRepository.save(consent);

        return ConsentResponse.builder()
                .consentGiven(true)
                .consentVersion(savedConsent.getConsentVersion())
                .givenAt(savedConsent.getGivenAt())
                .build();
    }

    public ConsentResponse getConsent(String userId) {

        Consent consent = consentRepository
                .findByUserId(userId)
                .orElse(null);

        if (consent == null) {
            return ConsentResponse.builder()
                    .consentGiven(false)
                    .build();
        }

        return ConsentResponse.builder()
                .consentGiven(true)
                .consentVersion(consent.getConsentVersion())
                .givenAt(consent.getGivenAt())
                .build();
    }
}