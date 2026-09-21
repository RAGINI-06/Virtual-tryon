package com.arose.service;
import com.arose.storage.StorageService;
import org.springframework.web.multipart.MultipartFile;
import com.arose.dto.profile.ProfileResponse;
import com.arose.dto.profile.ProfileUpdateRequest;
import com.arose.entity.BodyMeasurement;
import com.arose.entity.MeasurementUnit;
import com.arose.entity.User;
import com.arose.entity.UserProfile;
import com.arose.repository.BodyMeasurementRepository;
import com.arose.repository.UserProfileRepository;
import com.arose.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.Resource;
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final BodyMeasurementRepository bodyMeasurementRepository;
    private final StorageService storageService;

    public ProfileService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            BodyMeasurementRepository bodyMeasurementRepository,
            StorageService storageService
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.bodyMeasurementRepository = bodyMeasurementRepository;
        this.storageService = storageService;
    }

    @Transactional
    public ProfileResponse getProfile(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseGet(() -> createProfile(user));

        BodyMeasurement measurements = bodyMeasurementRepository
                .findByUserId(userId)
                .orElseGet(() -> createMeasurements(user));

        return buildResponse(
                user,
                profile,
                measurements
        );
    }

    @Transactional
    public ProfileResponse updateProfile(
            String userId,
            ProfileUpdateRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseGet(() -> createProfile(user));

        BodyMeasurement measurements = bodyMeasurementRepository
                .findByUserId(userId)
                .orElseGet(() -> createMeasurements(user));

        if (request.getName() != null &&
                !request.getName().isBlank()) {

            user.setName(request.getName());
        }

        if (request.getEmail() != null &&
                !request.getEmail().isBlank()) {

            if (!request.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {

                throw new IllegalArgumentException(
                        "Email already registered"
                );
            }

            user.setEmail(request.getEmail());
        }

        if (request.getHeight() != null) {
            profile.setHeight(request.getHeight());
        }

        if (request.getWeight() != null) {
            profile.setWeight(request.getWeight());
        }

        if (request.getUnit() != null &&
                !request.getUnit().isBlank()) {

            profile.setUnit(
                    MeasurementUnit.valueOf(
                            request.getUnit().toUpperCase()
                    )
            );
        }

        if (request.getBust() != null) {
            measurements.setBust(request.getBust());
        }

        if (request.getWaist() != null) {
            measurements.setWaist(request.getWaist());
        }

        if (request.getHips() != null) {
            measurements.setHips(request.getHips());
        }

        if (request.getShoulder() != null) {
            measurements.setShoulder(request.getShoulder());
        }

        if (request.getInseam() != null) {
            measurements.setInseam(request.getInseam());
        }

        userRepository.save(user);
        userProfileRepository.save(profile);
        bodyMeasurementRepository.save(measurements);

        return buildResponse(
                user,
                profile,
                measurements
        );
    }

    private UserProfile createProfile(User user) {

        UserProfile profile = UserProfile.builder()
                .user(user)
                .unit(MeasurementUnit.CM)
                .build();

        return userProfileRepository.save(profile);
    }

    private BodyMeasurement createMeasurements(User user) {

        BodyMeasurement measurements = BodyMeasurement.builder()
                .user(user)
                .build();

        return bodyMeasurementRepository.save(measurements);
    }

    private ProfileResponse buildResponse(
            User user,
            UserProfile profile,
            BodyMeasurement measurements
    ) {

        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profilePhotoUrl(profile.getProfilePhotoUrl())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .bust(measurements.getBust())
                .waist(measurements.getWaist())
                .hips(measurements.getHips())
                .shoulder(measurements.getShoulder())
                .inseam(measurements.getInseam())
                .unit(profile.getUnit().name())
                .build();
    }
//    @Transactional
//    public ProfileResponse uploadProfilePhoto(
//            String userId,
//            MultipartFile file
//    ) {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() ->
//                        new RuntimeException("User not found")
//                );
//
//        UserProfile profile = userProfileRepository
//                .findByUserId(userId)
//                .orElseGet(() -> createProfile(user));
//
//        String oldPhoto = profile.getProfilePhotoUrl();
//
//        String newPhoto = storageService.store(
//                file,
//                "profiles"
//        );
//
//        profile.setProfilePhotoUrl(newPhoto);
//
//        userProfileRepository.save(profile);
//
//        // Delete old photo only after new photo is stored successfully
//        if (oldPhoto != null && !oldPhoto.isBlank()) {
//            try {
//                storageService.delete(oldPhoto);
//            } catch (Exception ignored) {
//                // Do not fail the request if old-file cleanup fails
//            }
//        }
//
//        BodyMeasurement measurements =
//                bodyMeasurementRepository
//                        .findByUserId(userId)
//                        .orElseGet(() -> createMeasurements(user));
//
//        return buildResponse(
//                user,
//                profile,
//                measurements
//        );
//    }
    @Transactional
    public ProfileResponse uploadProfilePhoto(
            String userId,
            MultipartFile file
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseGet(() -> createProfile(user));

        String oldPhoto = profile.getProfilePhotoUrl();

        String newPhoto = storageService.store(
                file,
                "profiles"
        );

        profile.setProfilePhotoUrl(newPhoto);

        userProfileRepository.save(profile);

        // Delete old photo only after new photo is stored successfully
        if (oldPhoto != null && !oldPhoto.isBlank()) {
            try {
                storageService.delete(oldPhoto);
            } catch (Exception ignored) {
                // Do not fail the request if old-file cleanup fails
            }
        }

        BodyMeasurement measurements =
                bodyMeasurementRepository
                        .findByUserId(userId)
                        .orElseGet(() -> createMeasurements(user));

        return buildResponse(
                user,
                profile,
                measurements
        );
    }
    @Transactional
    public void deleteProfilePhoto(String userId) {

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found")
                );

        String photo = profile.getProfilePhotoUrl();

        if (photo == null || photo.isBlank()) {
            return;
        }

        // Remove photo reference from database
        profile.setProfilePhotoUrl(null);
        userProfileRepository.save(profile);

        // Remove actual file from storage
        try {
            storageService.delete(photo);
        } catch (Exception ignored) {
            // Don't fail the API if physical file cleanup fails
        }
    }
    public Resource getProfilePhoto(String userId) {

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found")
                );

        String photoKey = profile.getProfilePhotoUrl();

        if (photoKey == null || photoKey.isBlank()) {
            throw new RuntimeException(
                    "Profile photo not found"
            );
        }

        return storageService.load(photoKey);
    }
}