package com.example.movieapi.configuration;

import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class QuartzVirtualThreadConfig {

    @Bean
    public SchedulerFactoryBeanCustomizer quartzSchedulerCustomizer(TaskExecutor taskExecutor) {
        return schedulerFactoryBean -> schedulerFactoryBean.setTaskExecutor(taskExecutor);
    }
}
