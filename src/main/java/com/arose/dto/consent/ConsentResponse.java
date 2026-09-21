package com.arose.dto.consent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ConsentResponse {

    private boolean consentGiven;
    private String consentVersion;
    private LocalDateTime givenAt;
}