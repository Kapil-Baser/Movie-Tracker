package com.example.movieapi.event;

import com.example.movieapi.service.EmailVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserRegisteredListener {

    private final EmailVerificationService emailVerificationService;


    public UserRegisteredListener(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @EventListener
    public void sendConfirmationToken(UserRegisteredEvent event) {
        log.info("Generating token for user: {}", event.user().getUsername());
        String token = emailVerificationService.createToken(event.user().getEmail());
        log.info("Successfully generated token: {}", token);
        // TODO: Send email to user

    }
}
