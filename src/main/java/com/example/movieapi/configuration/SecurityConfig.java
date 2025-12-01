package com.example.movieapi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(Customizer.withDefaults())
                .formLogin(httpForm -> httpForm
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/movies")
                        .failureUrl("/auth/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/movies")
                        .permitAll()
                    )
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers("/api/v1/admin/**")
                            .hasRole("ADMIN")
                            .requestMatchers("/user/**")
                            .hasRole("USER")
                            .requestMatchers("/auth/**")
                            .permitAll()
                            .requestMatchers("/webjars/**","/movies", "/movies/upcoming")
                            .permitAll()
                            .anyRequest()
                            .authenticated();
                })
                .build();
    }
}
