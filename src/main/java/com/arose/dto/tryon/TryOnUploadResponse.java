package com.arose.dto.tryon;

import com.arose.entity.TryOnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TryOnUploadResponse {

    private String requestId;
    private TryOnStatus status;
    private String message;
}