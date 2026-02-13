package com.example.movieapi.event;

import com.example.movieapi.exception.EmailFailedException;
import com.example.movieapi.service.MailService;
import com.example.movieapi.service.PasswordResetTokenService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PasswordResetEventListener {

    private final PasswordResetTokenService passwordResetTokenService;
    private final MailService mailService;

    @Autowired
    public PasswordResetEventListener(PasswordResetTokenService resetTokenService, MailService mailService) {
        this.passwordResetTokenService = resetTokenService;
        this.mailService = mailService;
    }

    @EventListener
    public void sendResetTokenMail(PasswordResetEvent event) {

        String token = passwordResetTokenService.createToken(event.email());
        if (token != null && !token.isEmpty()) {
            String email = event.email();
            String link = "http://localhost:8080/auth/resetPassword?token=" + token;

            try {
                mailService.sendPasswordResetEmail(email, link);
                log.info("Password reset token: {} for user: {}", token, event.email());
            } catch (MessagingException e) {
                log.error("Error while sending password reset email", e);
                throw new EmailFailedException(e.getMessage());
            }
        }
    }
}
