package com.example.movieapi.event;

import com.example.movieapi.exception.EmailFailedException;
import com.example.movieapi.service.EmailVerificationService;
import com.example.movieapi.service.MailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserRegisteredListener {

    private final EmailVerificationService emailVerificationService;
    private final MailService mailService;

    @Autowired
    public UserRegisteredListener(EmailVerificationService emailVerificationService, MailService mailService) {
        this.emailVerificationService = emailVerificationService;
        this.mailService = mailService;
    }

    @EventListener
    public void sendConfirmationToken(UserRegisteredEvent event) {
        log.info("Generating token for user: {}", event.user().getUsername());
        String token = emailVerificationService.createToken(event.user().getEmail());
        log.info("Successfully generated token: {}", token);
        // TODO: Send email to user
        String to = event.user().getEmail();
        String name = event.user().getUsername();
        String link = "http://localhost:8080/auth/confirm?token=" + token;

        try {
            mailService.sendRegistrationEmail(to, name, link);
        } catch (MessagingException e) {
            log.error("Error while sending registration email", e);
            throw new EmailFailedException(e.getMessage());
        }
    }
}
