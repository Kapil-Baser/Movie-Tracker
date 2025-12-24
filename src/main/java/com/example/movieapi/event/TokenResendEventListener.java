package com.example.movieapi.event;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.ConfirmationToken;
import com.example.movieapi.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenResendEventListener {
    private final TokenService tokenService;

    public TokenResendEventListener(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @EventListener
    public void resendConfirmationToken(TokenResendEvent event) {
        AppUser user = event.user();
        log.info("Generating new token for user: {}", user.getEmail());
        //String token = tokenService.createEmailConfirmationToken(event.user());
        //log.info("New token generated: {}", token);

        // TODO: send mail

    }
}
