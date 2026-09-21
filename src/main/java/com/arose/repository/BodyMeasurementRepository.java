package com.arose.repository;

import com.arose.entity.BodyMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BodyMeasurementRepository
        extends JpaRepository<BodyMeasurement, String> {

    Optional<BodyMeasurement> findByUserId(String userId);

    boolean existsByUserId(String userId);
}