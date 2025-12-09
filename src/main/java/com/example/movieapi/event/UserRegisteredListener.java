package com.example.movieapi.event;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.ConfirmationToken;
import com.example.movieapi.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserRegisteredListener {

    private final TokenService tokenService;


    public UserRegisteredListener(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @EventListener
    public void sendConfirmationToken(UserRegisteredEvent event) {
        log.info("Generating token for user: {}", event.user().getUsername());
        ConfirmationToken token = tokenService.generateConfirmationToken(event.user());
        log.info("Successfully generated token: {}", token.getToken());
        // TODO: Send email to user

    }
}
