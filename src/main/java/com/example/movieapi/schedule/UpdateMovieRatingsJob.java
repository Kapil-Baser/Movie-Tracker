package com.example.movieapi.schedule;

import com.example.movieapi.service.MovieSyncService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class UpdateMovieRatingsJob implements Job {

    private final MovieSyncService movieSyncService;

    public UpdateMovieRatingsJob(MovieSyncService movieSyncService) {
        this.movieSyncService = movieSyncService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        movieSyncService.updateMovieRatings();
    }
}
