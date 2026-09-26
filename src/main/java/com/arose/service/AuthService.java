
        package com.arose.service;

import com.arose.dto.auth.LoginRequest;
import com.arose.dto.auth.LoginResponse;
import com.arose.dto.auth.RegisterRequest;
import com.arose.entity.User;
import com.arose.repository.UserRepository;
import com.arose.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public User register(RegisterRequest request) {

        // Check whether email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);

        /*
         * EMAIL TEMPORARILY DISABLED
         *
         * Render Free blocks outbound SMTP connections
         * to Gmail SMTP port 587.
         *
         * Registration itself works without email.
         *
         * We will enable this again later when an email
         * provider such as Resend is configured.
         *
         * DO NOT DELETE EmailService.java.
         */

        /*
        try {
            emailService.sendRegistrationConfirmation(
                    savedUser.getEmail(),
                    savedUser.getName()
            );
        } catch (Exception e) {
            System.err.println(
                    "Registration email could not be sent to "
                            + savedUser.getEmail()
            );
            e.printStackTrace();
        }
        */

        return savedUser;
    }

    public LoginResponse login(LoginRequest request) {

        // Find user by email
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password")
                );

        // Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate JWT
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );

        // Return login response
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
