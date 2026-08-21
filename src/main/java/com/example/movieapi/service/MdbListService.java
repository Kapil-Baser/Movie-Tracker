package com.example.movieapi.service;

import com.example.movieapi.model.mdblist.MdbListMovie;
import com.example.movieapi.model.mdblist.MdbListMovies;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class MdbListService {

    @Value("${mdblist.api.key}")
    private String mdbListApiKey;

    private final RestClient mdbListClient;

    public MdbListService(@Qualifier("mdbListServiceClient") RestClient restClient) {
        this.mdbListClient = restClient;
    }

    public String getHorrorMovies() {
        return mdbListClient.get()
                .uri(uriBuilder -> uriBuilder.path("/catalog/movie")
                    .queryParam("apikey", mdbListApiKey)
                    .queryParam("genre", "horror")
                    .queryParam("year_min", 2026)
                    .queryParam("limit", 20)
                    .queryParam("append_to_response", "ratings")
                    .build()
        )
                .retrieve()
                .body(String.class);
    }

    public MdbListMovies getOfficialList() {
        return mdbListClient.get()
                .uri(uriBuilder -> uriBuilder.path("/lists/official/popular/items")
                        .queryParam("apikey", mdbListApiKey)
                        .queryParam("mediatype", "movie")
                        .queryParam("append_to_response", "ratings")
                        .build()
                )
                .retrieve()
                .body(MdbListMovies.class);
    }

    public MdbListMovies getListItems(String username, String listName) {
        return mdbListClient.get().uri(uriBuilder -> uriBuilder
                        .path("/lists/{username}/{listName}/items")
                        .queryParam("apikey", mdbListApiKey)
                        .queryParam("limit", 50)
                        .queryParam("append_to_response", "ratings")
                        .queryParam("mediatype", "movie")
                        .build(username, listName))
                .retrieve()
                .body(MdbListMovies.class);
    }
}
