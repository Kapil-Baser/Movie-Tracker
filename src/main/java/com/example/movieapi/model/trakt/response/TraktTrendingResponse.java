package com.example.movieapi.model.trakt.response;

import com.example.movieapi.model.trakt.model.TraktMovie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TraktTrendingResponse {
    private int watchers;
    private TraktMovie movie;
}
