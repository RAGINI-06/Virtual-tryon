package com.arose.repository;

import com.arose.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentRepository extends JpaRepository<Consent, String> {

    Optional<Consent> findByUserId(String userId);

    boolean existsByUserId(String userId);
}