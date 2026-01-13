package com.example.movieapi.schedule;

import com.example.movieapi.entity.Movie;
import com.example.movieapi.event.MovieReleasedEvent;
import com.example.movieapi.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MovieOutForStreamingPublisher implements Job {

    private final ApplicationEventPublisher movieReleasedEventPublisher;
    private final MovieService movieService;

    @Autowired
    public MovieOutForStreamingPublisher(ApplicationEventPublisher movieReleasedEventPublisher, MovieService movieService) {
        this.movieReleasedEventPublisher = movieReleasedEventPublisher;
        this.movieService = movieService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        List<Movie> moviesStreamingToday = movieService.moviesOutForStreamingToday();
        if(moviesStreamingToday.isEmpty()){
            log.info("No movies are out for streaming today");
            return;
        }

        moviesStreamingToday.forEach(movie -> movieReleasedEventPublisher.publishEvent(new MovieReleasedEvent(movie)));
        log.info("There are {} movies out for streaming today", moviesStreamingToday.size());
    }
}
