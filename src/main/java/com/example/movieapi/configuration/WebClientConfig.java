package com.example.movieapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class WebClientConfig {

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Value("${tmdb.base.url}")
    private String tmdbBaseUrl;

    @Value("${mdblist.base.url}")
    private String mdbListBaseUrl;

    @Value(("${trakt.client}"))
    private String traktClientId;

    @Value("${trakt.base.url}")
    private String traktBaseUrl;

    @Bean(name = "tmdbServiceClient")
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseUrl)
                .defaultHeader("Authorization", "Bearer " + tmdbApiKey)
                .defaultHeader("Accept", "authenticate/json")
                .build();
    }

    @Bean(name = "mdbListServiceClient")
    public RestClient mdbListRestClient() {
        return RestClient.builder()
                .baseUrl(mdbListBaseUrl)
                .build();
    }

    @Bean(name = "traktServiceClient")
    public RestClient traktRestClient() {
        return RestClient.builder()
                .baseUrl(traktBaseUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.set("User-Agent", "MovieTracker/1.0");
                    httpHeaders.set("trakt-api-version", "2");
                    httpHeaders.set("trakt-api-key", traktClientId);
                })
                .build();
    }
}
