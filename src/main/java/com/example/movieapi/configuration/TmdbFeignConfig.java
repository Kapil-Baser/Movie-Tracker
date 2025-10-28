package com.example.movieapi.configuration;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TmdbFeignConfig {

    private final String accessToken;

    public TmdbFeignConfig(@Value("${api.key}") String accessToken) {
        this.accessToken = accessToken;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer " + accessToken);
            requestTemplate.header("accept", "application/json");
        };
    }
}
