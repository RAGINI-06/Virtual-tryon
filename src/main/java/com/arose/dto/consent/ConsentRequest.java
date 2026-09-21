package com.arose.dto.consent;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsentRequest {

    @AssertTrue(message = "Consent is required")
    private boolean consentGiven;
}