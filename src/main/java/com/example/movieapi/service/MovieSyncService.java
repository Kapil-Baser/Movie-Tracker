package com.example.movieapi.service;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.mapper.MovieMapper;
import com.example.movieapi.model.TmdbCountryRelease;
import com.example.movieapi.model.TmdbReleaseDatesResponse;
import com.example.movieapi.model.response.MovieResultResponse;
import com.example.movieapi.utility.ReleaseTypeUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class MovieSyncService {

    private final TmdbService tmdbService;
    private final MovieService movieService;
    private final MovieMapper movieMapper;
    private final MovieCollectionService movieCollectionService;

    @Autowired
    public MovieSyncService(TmdbService tmdbService, MovieService movieService, MovieMapper movieMapper, MovieCollectionService movieCollectionService) {
        this.tmdbService = tmdbService;
        this.movieService = movieService;
        this.movieMapper = movieMapper;
        this.movieCollectionService = movieCollectionService;
    }

    @Transactional
    public List<MovieDto> syncUpcomingMoviesToCollection() {

        // Fetching the collection from API
        List<MovieResultResponse> movieResults = tmdbService.getUpcomingMovies();

        if (movieResults == null || movieResults.isEmpty()) {
            log.warn("Failed to fetch upcoming movies");
            return Collections.emptyList();
        }

        List<Movie> upcomingMovies = movieService.saveMovies(movieResults);

        movieCollectionService.addMoviesToCollection("Upcoming", upcomingMovies);

        log.info("Saved {} movies in upcoming movies Collection", upcomingMovies.size());

        return movieMapper.toMovieDto(upcomingMovies);
    }

    @Transactional
    public List<MovieDto> syncNowPlayingMovies() {

        LocalDate currentDate = LocalDate.now();

        List<MovieResultResponse> movieResults = tmdbService.discoverMovies(currentDate.getYear(), currentDate.minusMonths(2), ReleaseTypeUtil.THEATRICAL);

        // Saving the movies which are new and does not yet exist in the database
        List<Movie> nowPlayingMovies = movieService.saveMovies(movieResults);

        // Making a new now playing collection
        movieCollectionService.addMoviesToCollection("Now Playing", nowPlayingMovies);

        return movieMapper.toMovieDto(nowPlayingMovies);
    }

    public List<MovieDto> syncDigitalReleaseDates() {
        // Getting only the movies which do not have a digital release date
        List<Movie> movies = movieService.getMoviesWithNoDigitalReleaseDate();

        List<Movie> updatedMovies = movies.stream()
                .map(this::fetchReleaseDateFromTmdbAndUpdate)
                .toList();

        log.info("Updated total {} movies", updatedMovies.size());

        return movieMapper.toMovieDto(updatedMovies);
    }

    @Transactional
    private Movie fetchReleaseDateFromTmdbAndUpdate(Movie movie) {
        TmdbReleaseDatesResponse response = tmdbService.getReleaseDatesByMovieId(movie.getId());
        if (response == null || response.getResults().isEmpty()) {
            log.warn("Failed to fetch release dates for movie {}", movie.getTitle());
            return movie;
        }

        List<TmdbCountryRelease> allReleases = response.getResults();

        return movieService.updateUsReleaseDates(movie, allReleases);
    }
}
