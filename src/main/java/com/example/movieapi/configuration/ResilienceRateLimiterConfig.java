package com.example.movieapi.configuration;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceRateLimiterConfig {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(2)
                .limitRefreshPeriod(Duration.ofSeconds(3))
                .timeoutDuration(Duration.ofSeconds(300)) // 200 requests per 5 minutes
                .build();

        return RateLimiterRegistry.of(config);
    }

    @Bean
    public RateLimiter mdbListRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("mdbListRateLimiter");
    }
}
