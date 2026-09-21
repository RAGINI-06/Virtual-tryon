
package com.arose.service;
import com.arose.dto.tryon.TryOnHistoryResponse;
import com.arose.ai.AIService;
import com.arose.dto.tryon.TryOnUploadResponse;
import com.arose.entity.TryOnRequest;
import com.arose.entity.TryOnStatus;
import com.arose.entity.User;
import com.arose.repository.ConsentRepository;
import com.arose.repository.TryOnRequestRepository;
import com.arose.repository.UserRepository;
import com.arose.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.arose.exception.ResourceNotFoundException;
@Service
public class TryOnService {

    private final TryOnRequestRepository tryOnRequestRepository;
    private final UserRepository userRepository;
    private final ConsentRepository consentRepository;
    private final StorageService storageService;
    private final AIService aiService;

    public TryOnService(
            TryOnRequestRepository tryOnRequestRepository,
            UserRepository userRepository,
            ConsentRepository consentRepository,
            StorageService storageService,
            AIService aiService
    ) {
        this.tryOnRequestRepository = tryOnRequestRepository;
        this.userRepository = userRepository;
        this.consentRepository = consentRepository;
        this.storageService = storageService;
        this.aiService = aiService;
    }

    public TryOnUploadResponse uploadPhotos(
            String userId,
            MultipartFile personPhoto,
            MultipartFile garmentPhoto
    ) {

        // --------------------------------------------------
        // 1. Validate uploaded images
        // --------------------------------------------------

        validateImage(personPhoto, "Person photo");
        validateImage(garmentPhoto, "Garment photo");

        // --------------------------------------------------
        // 2. Check user consent
        // --------------------------------------------------

        if (!consentRepository.existsByUserId(userId)) {
            throw new IllegalStateException(
                    "Consent is required before using virtual try-on"
            );
        }

        // --------------------------------------------------
        // 3. Find current user
        // --------------------------------------------------

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        String personStorageKey = null;
        String garmentStorageKey = null;
        String resultStorageKey = null;

        TryOnRequest savedRequest = null;

        try {

            // --------------------------------------------------
            // 4. Store person image
            // --------------------------------------------------

            personStorageKey = storageService.store(
                    personPhoto,
                    "persons"
            );

            // --------------------------------------------------
            // 5. Store garment image
            // --------------------------------------------------

            garmentStorageKey = storageService.store(
                    garmentPhoto,
                    "garments"
            );

            // --------------------------------------------------
            // 6. Create try-on request
            // --------------------------------------------------

            TryOnRequest request = TryOnRequest.builder()
                    .user(user)
                    .sourcePhotoUrl(personStorageKey)
                    .garmentPhotoUrl(garmentStorageKey)
                    .status(TryOnStatus.PROCESSING)
                    .build();

            savedRequest =
                    tryOnRequestRepository.save(request);

            // --------------------------------------------------
            // 7. Read uploaded images
            // --------------------------------------------------

            byte[] personImage =
                    personPhoto.getBytes();

            byte[] garmentImage =
                    garmentPhoto.getBytes();

            // --------------------------------------------------
            // 8. Determine MIME types
            // --------------------------------------------------

            String personMimeType =
                    getMimeType(personPhoto);

            String garmentMimeType =
                    getMimeType(garmentPhoto);

            // --------------------------------------------------
            // 9. Send images to Gemini
            // --------------------------------------------------

            byte[] generatedImage =
                    aiService.generateTryOn(
                            personImage,
                            personMimeType,
                            garmentImage,
                            garmentMimeType
                    );

            // --------------------------------------------------
            // 10. Validate Gemini response
            // --------------------------------------------------

            if (generatedImage == null ||
                    generatedImage.length == 0) {

                throw new RuntimeException(
                        "Gemini returned an empty image"
                );
            }

            // --------------------------------------------------
            // 11. Convert generated image to MultipartFile
            // --------------------------------------------------

            MultipartFile resultFile =
                    new GeneratedImageMultipartFile(
                            generatedImage,
                            "try-on-result.png",
                            "image/png"
                    );

            // --------------------------------------------------
            // 12. Store generated image
            // --------------------------------------------------

            resultStorageKey =
                    storageService.store(
                            resultFile,
                            "results"
                    );

            // --------------------------------------------------
            // 13. Mark request as completed
            // --------------------------------------------------

            savedRequest.setResultUrl(
                    resultStorageKey
            );

            savedRequest.setStatus(
                    TryOnStatus.COMPLETED
            );

            savedRequest.setCompletedAt(
                    LocalDateTime.now()
            );

            savedRequest.setFailureReason(null);

            tryOnRequestRepository.save(
                    savedRequest
            );

            // --------------------------------------------------
            // 14. Return successful response
            // --------------------------------------------------

            return TryOnUploadResponse.builder()
                    .requestId(savedRequest.getId())
                    .status(savedRequest.getStatus())
                    .message(
                            "Virtual try-on generated successfully"
                    )
                    .build();

        } catch (Exception e) {

            // --------------------------------------------------
            // 15. Mark request as FAILED
            // --------------------------------------------------

            if (savedRequest != null) {

                try {

                    savedRequest.setStatus(
                            TryOnStatus.FAILED
                    );

                    savedRequest.setFailureReason(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unknown error during try-on generation"
                    );

                    tryOnRequestRepository.save(
                            savedRequest
                    );

                } catch (Exception ignored) {

                    // Do not hide the original exception
                }
            }

            // --------------------------------------------------
            // 16. Clean up stored person image
            // --------------------------------------------------

            if (personStorageKey != null) {

                try {
                    storageService.delete(
                            personStorageKey
                    );
                } catch (Exception ignored) {
                    // Ignore cleanup failure
                }
            }

            // --------------------------------------------------
            // 17. Clean up stored garment image
            // --------------------------------------------------

            if (garmentStorageKey != null) {

                try {
                    storageService.delete(
                            garmentStorageKey
                    );
                } catch (Exception ignored) {
                    // Ignore cleanup failure
                }
            }

            // --------------------------------------------------
            // 18. Clean up generated result if necessary
            // --------------------------------------------------

            if (resultStorageKey != null) {

                try {
                    storageService.delete(
                            resultStorageKey
                    );
                } catch (Exception ignored) {
                    // Ignore cleanup failure
                }
            }

            // --------------------------------------------------
            // 19. Return original failure
            // --------------------------------------------------

            throw new RuntimeException(
                    "Virtual try-on generation failed",
                    e
            );
        }
    }

    // =========================================================
    // IMAGE VALIDATION
    // =========================================================

    private void validateImage(
            MultipartFile file,
            String fieldName
    ) {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        String contentType =
                file.getContentType();

        String filename =
                file.getOriginalFilename();

        boolean validContentType =
                contentType != null &&
                        contentType.startsWith("image/");

        boolean validExtension =
                filename != null &&
                        filename.toLowerCase().matches(
                                ".*\\.(jpg|jpeg|png|webp)$"
                        );

        if (!validContentType &&
                !validExtension) {

            throw new IllegalArgumentException(
                    fieldName +
                            " must be a JPG, JPEG, PNG, or WEBP image"
            );
        }
    }

    // =========================================================
    // MIME TYPE
    // =========================================================

    private String getMimeType(
            MultipartFile file
    ) {

        String contentType =
                file.getContentType();

        // Normal browser upload
        if (contentType != null &&
                contentType.startsWith("image/") &&
                !contentType.equalsIgnoreCase("File")) {

            return contentType;
        }

        // Fallback for Postman / unusual clients
        String filename =
                file.getOriginalFilename();

        if (filename == null) {

            throw new IllegalArgumentException(
                    "Could not determine image type"
            );
        }

        String lower =
                filename.toLowerCase();

        if (lower.endsWith(".jpg") ||
                lower.endsWith(".jpeg")) {

            return "image/jpeg";
        }

        if (lower.endsWith(".png")) {

            return "image/png";
        }

        if (lower.endsWith(".webp")) {

            return "image/webp";
        }

        throw new IllegalArgumentException(
                "Unsupported image type"
        );
    }
    public java.util.List<TryOnHistoryResponse> getHistory(
            String userId
    ) {

        return tryOnRequestRepository
                .findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(request ->
                        TryOnHistoryResponse.builder()
                                .requestId(request.getId())
                                .status(request.getStatus())
                                .resultUrl(request.getResultUrl())
                                .createdAt(request.getCreatedAt())
                                .completedAt(request.getCompletedAt())
                                .build()
                )
                .toList();
    }
    @Transactional
    public void deleteTryOn(
            String userId,
            String requestId
    ) {

        TryOnRequest request =
                tryOnRequestRepository
                        .findByIdAndUserIdAndDeletedAtIsNull(
                                requestId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Try-on request not found"
                                )

                        );

        String personStorageKey =
                request.getSourcePhotoUrl();

        String garmentStorageKey =
                request.getGarmentPhotoUrl();

        String resultStorageKey =
                request.getResultUrl();

        request.setDeletedAt(
                LocalDateTime.now()
        );

        tryOnRequestRepository.save(request);

        deleteFileSafely(personStorageKey);
        deleteFileSafely(garmentStorageKey);
        deleteFileSafely(resultStorageKey);
    }
    private void deleteFileSafely(
            String storageKey
    ) {

        if (storageKey == null ||
                storageKey.isBlank()) {

            return;
        }

        try {
            storageService.delete(storageKey);
        } catch (Exception ignored) {
            // Database record is already soft-deleted.
            // File cleanup failure should not expose
            // internal storage errors to the user.
        }
    }
}