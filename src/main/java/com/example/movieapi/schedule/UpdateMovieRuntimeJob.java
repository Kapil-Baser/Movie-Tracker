package com.example.movieapi.schedule;

import com.example.movieapi.service.MovieSyncService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class UpdateMovieRuntimeJob implements Job {

    private final MovieSyncService movieSyncService;

    public UpdateMovieRuntimeJob(MovieSyncService movieSyncService) {
        this.movieSyncService = movieSyncService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
       movieSyncService.updateMovieRuntime();
    }
}
