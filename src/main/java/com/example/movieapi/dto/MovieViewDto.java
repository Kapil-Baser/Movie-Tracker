package com.example.movieapi.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovieViewDto {
    MovieDto movieDto;
    boolean isFavorited;
    boolean isInWatchList;
    boolean isSubscribed;
    boolean isSubscribable;
    boolean isWatched;
}
