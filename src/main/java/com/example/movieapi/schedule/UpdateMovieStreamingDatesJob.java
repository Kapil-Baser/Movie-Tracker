package com.example.movieapi.schedule;

import com.example.movieapi.service.MovieSyncService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class UpdateMovieStreamingDatesJob implements Job {

    private final MovieSyncService movieSyncService;

    public UpdateMovieStreamingDatesJob(MovieSyncService movieSyncService) {
        this.movieSyncService = movieSyncService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //movieSyncService.fetchAndSyncDigitalReleaseDates();
    }
}
