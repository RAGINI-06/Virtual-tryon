package com.arose.repository;

import com.arose.entity.GuestSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestSessionRepository
        extends JpaRepository<GuestSession, String> {

    Optional<GuestSession> findByGuestToken(String guestToken);
}