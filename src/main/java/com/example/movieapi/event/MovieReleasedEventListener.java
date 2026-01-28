package com.example.movieapi.event;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.MovieSubscription;
import com.example.movieapi.entity.Provider;
import com.example.movieapi.exception.GoogleCalendarException;
import com.example.movieapi.repository.MovieSubscriptionRepository;
import com.example.movieapi.service.GoogleCalendarService;
import com.example.movieapi.service.MailService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class MovieReleasedEventListener {

    private final MovieSubscriptionRepository movieSubscriptionRepository;
    private final MailService mailService;
    private final GoogleCalendarService googleCalendarService;

    @Autowired
    public MovieReleasedEventListener(MovieSubscriptionRepository movieSubscriptionRepository, MailService mailService, GoogleCalendarService googleCalendarService) {
        this.movieSubscriptionRepository = movieSubscriptionRepository;
        this.mailService = mailService;
        this.googleCalendarService = googleCalendarService;
    }

    @Async
    @EventListener
    @Transactional
    public void onMovieReleasedEvent(MovieReleasedEvent event) {
        Movie releasedMovie = event.movie();
        List<MovieSubscription> movieSubscriptions = movieSubscriptionRepository.findAllByMovieAndNotifiedFalse(releasedMovie);
        if (movieSubscriptions.isEmpty()) {
            log.info("No movie subscriptions found for released movies.");
            return;
        }

        for (MovieSubscription movieSubscription : movieSubscriptions) {
            AppUser user = movieSubscription.getUser();
            if (user != null) {
                log.info("User {} found for released movie subscription {}. Sending Email to - {}", user.getUsername(), movieSubscription.getMovie().getTitle(), user.getEmail());

                // Sending Google calendar event
                if (user.getProvider() == Provider.GOOGLE) {
                    try {
                        googleCalendarService.createMovieReleasedEvent(user, releasedMovie.getTitle());
                        log.info("Calendar event for movie {} successfully created for user {}", releasedMovie.getTitle(), user.getEmail());
                    } catch (IOException e) {
                        throw new GoogleCalendarException(e.getMessage());
                    }
                }
            }
            movieSubscription.setNotified(true);
            movieSubscription.setNotificationSentAt(LocalDateTime.now());
            movieSubscriptionRepository.save(movieSubscription);


        }
    }
}
