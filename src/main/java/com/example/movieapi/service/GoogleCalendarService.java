package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.exception.GoogleCalendarException;
import com.example.movieapi.repository.UserRepository;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class GoogleCalendarService {

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;
    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    private final UserRepository userRepository;

    public GoogleCalendarService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createEvent(String userEmail, String movieTitle) throws IOException {

        Optional<AppUser> optUser = userRepository.findByEmail(userEmail);

        if (optUser.isPresent()) {
            AppUser user = optUser.get();
            try {
                NetHttpTransport httpTransport = new NetHttpTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                Calendar calendarService = new Calendar.Builder(
                        httpTransport, jsonFactory, httpRequest -> httpRequest
                        .getHeaders()
                        .setAuthorization("Bearer " + user.getGoogleAccessToken())
                ).setApplicationName("Movie Tracker").build();

                Event.Creator creator = new Event.Creator();
                creator.setDisplayName("Movie Tracker");

                Event event = new Event()
                        .setSummary("Movie 'Title' is out now for streaming.")
                        .setDescription("'Movie' is now available for streaming and renting.")
                        .setCreator(creator);


                DateTime startDateTime = new DateTime(new Date());
                EventDateTime start = new EventDateTime()
                        .setDateTime(startDateTime)
                        .setTimeZone("America/Los_Angeles");
                event.setStart(start);

                DateTime endDateTime = new DateTime(new Date());
                EventDateTime end = new EventDateTime()
                        .setDateTime(endDateTime)
                        .setTimeZone("America/Los_Angeles");
                event.setEnd(end);

                String calendarId = "primary";
                calendarService.events().insert(calendarId, event).execute();

            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 401) {
                    String newAccessToken = refreshAccessToken(user.getGoogleRefreshToken())
                            .orElseThrow(() -> new ResourceAccessException("Failed to create Access Token"));
                    user.setGoogleAccessToken(newAccessToken);
                    userRepository.save(user);

                    createEvent(userEmail, movieTitle);
                } else {
                    throw new RuntimeException("Google Calendar API error", e);
                }

            } catch (IOException e) {
                throw new RuntimeException("Failed to create event", e);
            }
        }
    }

    public void createMovieReleasedEvent(AppUser user, String movieTitle) throws IOException {
        try {
            NetHttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Calendar calendarService = new Calendar.Builder(
                    httpTransport, jsonFactory, httpRequest -> httpRequest
                    .getHeaders()
                    .setAuthorization("Bearer " + user.getGoogleAccessToken())
            ).setApplicationName("Movie Tracker").build();

            Event.Creator creator = new Event.Creator();
            creator.setDisplayName("Movie Tracker");

            Event event = new Event()
                    .setSummary("Movie " + movieTitle + " is out now for streaming.")
                    .setDescription(movieTitle + " is now available for streaming and renting.")
                    .setCreator(creator);


            DateTime startDateTime = new DateTime(new Date());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setStart(start);

            DateTime endDateTime = new DateTime(new Date());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setEnd(end);

            String calendarId = "primary";
            calendarService.events().insert(calendarId, event).execute();

        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 401) {
                String newAccessToken = refreshAccessToken(user.getGoogleRefreshToken())
                        .orElseThrow(() -> new ResourceAccessException("Failed to create Access Token"));
                user.setGoogleAccessToken(newAccessToken);
                userRepository.save(user);

                createMovieReleasedEvent(user, movieTitle);
            } else {
                throw new GoogleCalendarException("Google Calendar API error", e);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to create event", e);
        }
    }

    private Optional<String> refreshAccessToken(String refreshToken) throws IOException {
        try {
            TokenResponse response = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(), new GsonFactory(),
                    refreshToken, clientId,
                    clientSecret).execute();

            log.info("Access token: {}", response.getAccessToken());

            return Optional.of(response.getAccessToken());
        } catch (TokenResponseException e) {
            if (e.getDetails() != null) {
                log.warn("Error: {}", e.getDetails().getError());
                if (e.getDetails().getErrorDescription() != null) {
                    log.error(e.getDetails().getErrorDescription());
                }
                if (e.getDetails().getErrorUri() != null) {
                    log.error(e.getDetails().getErrorUri());
                }
            } else {
                log.warn(e.getMessage());
            }
        }
        return Optional.empty();
    }

}
