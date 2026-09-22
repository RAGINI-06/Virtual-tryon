package com.arose.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    public void sendRegistrationConfirmation(
            String recipientEmail,
            String recipientName
    ) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(senderEmail);
            message.setTo(recipientEmail);
            message.setSubject("Welcome to AROSE");

            message.setText(
                    "Hi " + recipientName + ",\n\n" +
                            "Welcome to AROSE! 👗\n\n" +
                            "Your AROSE account has been successfully created.\n\n" +
                            "You can now log in and start using AROSE.\n\n" +
                            "Thank you for joining us!\n\n" +
                            "— AROSE Team"
            );

            System.out.println("Sending email...");
            System.out.println("From: " + senderEmail);
            System.out.println("To: " + recipientEmail);

            mailSender.send(message);

            System.out.println("EMAIL SENT SUCCESSFULLY");

        } catch (Exception e) {
            System.out.println("EMAIL FAILED");
            e.printStackTrace();

            throw e;
        }
    }}