package com.arose.controller;

import com.arose.dto.profile.ProfileResponse;
import com.arose.dto.profile.ProfileUpdateRequest;
import com.arose.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            Authentication authentication
    ) {
        String userId = authentication.getName();

        ProfileResponse response =
                profileService.getProfile(userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody ProfileUpdateRequest request
    ) {
        String userId = authentication.getName();

        ProfileResponse response =
                profileService.updateProfile(
                        userId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/photo")
    public ResponseEntity<ProfileResponse> uploadProfilePhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        String userId = authentication.getName();

        ProfileResponse response =
                profileService.uploadProfilePhoto(
                        userId,
                        file
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/photo")
    public ResponseEntity<Void> deleteProfilePhoto(
            Authentication authentication
    ) {
        String userId = authentication.getName();

        profileService.deleteProfilePhoto(userId);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/photo")
    public ResponseEntity<Resource> getProfilePhoto(
            Authentication authentication
    ) {
        String userId = authentication.getName();

        Resource photo = profileService.getProfilePhoto(userId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        String filename = photo.getFilename();

        if (filename != null) {
            String lower = filename.toLowerCase();

            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (lower.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (lower.endsWith(".webp")) {
                mediaType = MediaType.parseMediaType("image/webp");
            }
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                (filename == null
                                        ? "profile-photo"
                                        : filename) +
                                "\""
                )
                .contentType(mediaType)
                .body(photo);
    }
}