package com.example.movieapi.configuration;

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
                .withIdentity("Fetch_Movie_Digital_Release_Dates")
                .withDescription("Fetch digital release dates of movies")
                .build();
    }

    @Bean
    public Trigger fetchMovieDigitalReleaseDatesTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(fetchMovieDigitalReleaseDatesJob())
                .withIdentity("Fetch_Movie_Release_Dates_Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 * * ?"))
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
}
