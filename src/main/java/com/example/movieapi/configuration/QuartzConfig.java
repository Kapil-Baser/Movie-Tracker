package com.example.movieapi.configuration;

import com.example.movieapi.schedule.CheckForDigitalReleaseDatesJob;
import com.example.movieapi.schedule.MovieOutForStreamingPublisher;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail jobDetail() {
         return JobBuilder
                .newJob(CheckForDigitalReleaseDatesJob.class)
                .storeDurably()
                .withIdentity("Fetch_Movie_Digital_Release_Dates")
                .withDescription("Fetch digital release dates of movies")
                .build();
    }

    @Bean
    public Trigger trigger() {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail())
                .withIdentity("Fetch_Movie_Release_Dates_Trigger")
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(5))
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
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(10))
                .build();
    }
}
