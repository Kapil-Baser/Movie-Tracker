package com.example.movieapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class WebClientConfig {

    @Value("${api.key}")
    private String apiAccessToken;

    @Value("${trakt.api.key}")
    private String traktApiKey;
    @Value(("${trakt.client}"))
    private String traktClientId;
    @Value("${trakt.base.url}")
    private String traktBaseUrl;

    @Bean(name = "tmdbServiceClient")
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .defaultHeader("Authorization", "Bearer " + apiAccessToken)
                .defaultHeader("Accept", "authenticate/json")
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

    public HttpHeaders httpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("User-Agent", "MovieTracker/1.0");
        return httpHeaders;
    }

}
