package com.example.movieapi.schedule;

import com.example.movieapi.service.CollectionRefreshService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RefreshUpcomingCollectionJob implements Job {
    private final CollectionRefreshService collectionRefreshService;

    public RefreshUpcomingCollectionJob(CollectionRefreshService collectionRefreshService) {
        this.collectionRefreshService = collectionRefreshService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Collection Maintenance Job Started");
        collectionRefreshService.refreshUpcomingCollection();
        log.info("Collection Maintenance Job Completed");
    }
}
