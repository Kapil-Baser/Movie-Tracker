package com.example.movieapi.configuration;

import com.example.movieapi.schedule.CheckForDigitalReleaseDatesJob;
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
}
