package com.example.movieapi.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;


public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // If user is disabled
        if (exception instanceof DisabledException) {
            response.sendRedirect( "/auth/login?error=disabled");
        } else {
            super.setDefaultFailureUrl("/auth/login?error=invalid");
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
