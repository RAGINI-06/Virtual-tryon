package com.arose.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "consent_version", nullable = false)
    private String consentVersion;

    @Column(name = "given_at", nullable = false)
    private LocalDateTime givenAt;

    @PrePersist
    protected void onCreate() {
        givenAt = LocalDateTime.now();
    }
}