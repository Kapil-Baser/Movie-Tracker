package com.example.movieapi.configuration;

import com.example.movieapi.exception.CustomAuthenticationFailureHandler;
import com.example.movieapi.filters.CsrfLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Custom OAuth2 Authorization Request Resolver (for Google login)
    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.additionalParameters(params -> {
                    params.put("access_type", "offline");
                    params.put("prompt", "consent"); // Force consent to ensure refresh token is sent
                })
        );
        return resolver;
    }

    // Using this so I can show custom errors when authentication fails
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2AuthorizationRequestResolver resolver, CustomOAuth2LoginSuccessHandler successHandler) throws Exception {
        return http
                .httpBasic(Customizer.withDefaults())
                .addFilterAfter(new CsrfLoggingFilter(), CsrfFilter.class)

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/movies")
                        .failureHandler(authenticationFailureHandler())
                        .permitAll()
                )

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestResolver(resolver)
                        )
                        .failureHandler(authenticationFailureHandler())

                        .successHandler(successHandler)
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/movies")
                        .permitAll()
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/users/**").hasRole("USER")
                        .requestMatchers(
                                "/auth/**",
                                "/webjars/**",
                                "/movies/**",
                                "/register/**",
                                "/error"
                        )
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                        )

                .build();

    }

    /*@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2AuthorizationRequestResolver resolver, CustomOAuth2LoginSuccessHandler successHandler) throws Exception {
        return http
                .httpBasic(Customizer.withDefaults())
                .addFilterAfter(new CsrfLoggingFilter(), CsrfFilter.class)
                .formLogin(httpForm -> httpForm
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/movies")
                        .failureHandler(authenticationFailureHandler())
                        .permitAll())
                .oauth2Login(oauth -> oauth.authorizationEndpoint(auth -> auth.authorizationRequestResolver(resolver))
                    .failureHandler(authenticationFailureHandler())
                    .defaultSuccessUrl("/movies")
                        .successHandler(successHandler)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/movies")
                        .permitAll())
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers("/api/v1/admin/**")
                            .hasRole("ADMIN")
                            .requestMatchers("/user/**")
                            .hasRole("USER")
                            .requestMatchers("/auth/**")
                            .permitAll()
                            .requestMatchers("/webjars/**","/movies/**", "/register/**", "/error")
                            .permitAll()
                            .anyRequest()
                            .authenticated();
                })
                .build();
    }*/


}
