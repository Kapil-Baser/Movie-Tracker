package com.example.movieapi.event;

import com.example.movieapi.exception.EmailFailedException;
import com.example.movieapi.service.MailService;
import com.example.movieapi.service.PasswordResetTokenService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PasswordResetEventListener {

    @Value("${host}")
    private String host;
    private final PasswordResetTokenService passwordResetTokenService;
    private final MailService mailService;

    @Autowired
    public PasswordResetEventListener(PasswordResetTokenService resetTokenService, MailService mailService) {
        this.passwordResetTokenService = resetTokenService;
        this.mailService = mailService;
    }

    @Async("customExecutor")
    @EventListener
    public void sendResetTokenMail(PasswordResetEvent event) {

        String email = event.email();

        String token = passwordResetTokenService.createToken(email);

        String link = host + "/auth/resetPassword?token=" + token;

        try {
            mailService.sendPasswordResetEmail(email, link);
            log.info("Password reset token: {} for user: {}", token, email);
        } catch (MessagingException e) {
            log.error("Error while sending password reset email", e);
            throw new EmailFailedException(e.getMessage());
        }
    }
}
