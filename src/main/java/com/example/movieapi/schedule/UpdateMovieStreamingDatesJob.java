package com.example.movieapi.schedule;

import com.example.movieapi.service.MovieSyncService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateMovieStreamingDatesJob implements Job {

    private final MovieSyncService movieSyncService;

    public UpdateMovieStreamingDatesJob(MovieSyncService movieSyncService) {
        this.movieSyncService = movieSyncService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting Job for fetching movie streaming dates");
        movieSyncService.fetchAndSyncDigitalReleaseDates();
    }
}
