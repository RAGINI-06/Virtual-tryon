package com.arose.repository;

import com.arose.entity.TryOnRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TryOnRequestRepository
        extends JpaRepository<TryOnRequest, String> {

    List<TryOnRequest> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            String userId
    );

    Optional<TryOnRequest> findByIdAndUserIdAndDeletedAtIsNull(
            String id,
            String userId
    );
}