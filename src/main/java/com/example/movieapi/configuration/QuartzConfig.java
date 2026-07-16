package com.example.movieapi.configuration;

import com.example.movieapi.schedule.RefreshUpcomingCollectionJob;
import com.example.movieapi.schedule.UpdateMovieRuntimeJob;
import com.example.movieapi.schedule.UpdateMovieStreamingDatesJob;
import com.example.movieapi.schedule.MovieOutForStreamingPublisher;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail fetchMovieDigitalReleaseDatesJob() {
         return JobBuilder
                .newJob(UpdateMovieStreamingDatesJob.class)
                .storeDurably()
                 .requestRecovery()
                .withIdentity("Fetch_Movie_Digital_Release_Dates")
                .withDescription("Fetch digital release dates of movies")
                .build();
    }

    @Bean
    public Trigger fetchMovieDigitalReleaseDatesTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(fetchMovieDigitalReleaseDatesJob())
                .withIdentity("Fetch_Movie_Release_Dates_Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 * * ?")
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    @Bean
    public JobDetail movieOutForSteamingToday() {
        return JobBuilder
                .newJob(MovieOutForStreamingPublisher.class)
                .storeDurably()
                .withIdentity("Publish_Movies_Out_For_Streaming_Today")
                .withDescription("Publish movies which are out for streaming today")
                .build();
    }

    @Bean
    public Trigger movieReleasedEventPublisherTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(movieOutForSteamingToday())
                .withIdentity("Publish_Movies_Streaming_Today_Trigger")
                .withSchedule(SimpleScheduleBuilder.repeatHourlyForever(12))
                .build();
    }

    @Bean
    public JobDetail refreshUpcomingMoviesCollectionJob() {
        return JobBuilder
                .newJob(RefreshUpcomingCollectionJob.class)
                .storeDurably()
                .withIdentity("Refresh_Upcoming_Collection_Job")
                .withDescription("Refresh upcoming movies collection")
                .build();
    }

    @Bean
    public Trigger refreshUpcomingCollectionTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(refreshUpcomingMoviesCollectionJob())
                .withIdentity("Refresh_Upcoming_Collection_Trigger")
                .withSchedule(SimpleScheduleBuilder
                        .repeatHourlyForever(48)
                        .withMisfireHandlingInstructionIgnoreMisfires())
                .build();
    }

    @Bean
    public JobDetail updateMoviesMissingRuntimeJob() {
        return JobBuilder.newJob(UpdateMovieRuntimeJob.class)
                .storeDurably()
                .withIdentity("Update_Movies_Missing_Runtime")
                .withDescription("Job to scan and update movies which are missing runtime")
                .build();
    }

    @Bean
    public Trigger updateMoviesMissingRuntimeTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(updateMoviesMissingRuntimeJob())
                .withIdentity("Update_Movies_Missing_Runtime_Trigger")
                .withSchedule(SimpleScheduleBuilder
                        .repeatHourlyForever(120)
                        .withMisfireHandlingInstructionIgnoreMisfires())
                .build();
    }

}
