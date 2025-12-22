package com.example.movieapi.event;

import com.example.movieapi.service.PasswordResetTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PasswordResetEventListener {

    private final PasswordResetTokenService passwordResetTokenService;

    public PasswordResetEventListener(PasswordResetTokenService resetTokenService) {
        this.passwordResetTokenService = resetTokenService;
    }

    @EventListener
    public void sendResetTokenMail(PasswordResetEvent event) {
        // TODO mail password reset token
        String token = passwordResetTokenService.createToken(event.email());
        if (token != null && !token.isEmpty()) {
            // TODO: send mail
        }
        log.info("Password reset token: {} for user: {}", token, event.email());
    }
}
