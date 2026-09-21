package com.arose.controller;

import com.arose.dto.tryon.TryOnUploadResponse;
import com.arose.service.TryOnService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.arose.dto.tryon.TryOnHistoryResponse;

import java.util.List;
@RestController
@RequestMapping("/api/try-on")
public class TryOnController {

    private final TryOnService tryOnService;

    public TryOnController(TryOnService tryOnService) {
        this.tryOnService = tryOnService;
    }

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<TryOnUploadResponse> uploadPhotos(
            Authentication authentication,
            @RequestParam("personPhoto") MultipartFile personPhoto,
            @RequestParam("garmentPhoto") MultipartFile garmentPhoto
    ) {

        String userId = authentication.getName();

        TryOnUploadResponse response =
                tryOnService.uploadPhotos(
                        userId,
                        personPhoto,
                        garmentPhoto
                );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/history")
    public ResponseEntity<List<TryOnHistoryResponse>> getHistory(
            Authentication authentication
    ) {

        String userId = authentication.getName();

        List<TryOnHistoryResponse> history =
                tryOnService.getHistory(userId);

        return ResponseEntity.ok(history);
    }
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteTryOn(
            Authentication authentication,
            @PathVariable String requestId
    ) {

        String userId = authentication.getName();

        tryOnService.deleteTryOn(
                userId,
                requestId
        );

        return ResponseEntity.noContent().build();
    }
}