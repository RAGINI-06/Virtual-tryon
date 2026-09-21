package com.arose.dto.tryon;

import com.arose.entity.TryOnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TryOnHistoryResponse {

    private String requestId;

    private TryOnStatus status;

    private String resultUrl;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}