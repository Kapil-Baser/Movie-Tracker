package com.example.movieapi.controller;

import com.example.movieapi.dto.CalendarEventDto;
import com.example.movieapi.repository.UserRepository;
import com.example.movieapi.service.GoogleCalendarService;
import com.google.api.client.util.DateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
public class GoogleCalendarController {

    private GoogleCalendarService googleCalendarService;
    private UserRepository userRepository;

    @Autowired
    public GoogleCalendarController(GoogleCalendarService googleCalendarService, UserRepository userRepository) {
        this.googleCalendarService = googleCalendarService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public String createCalendarEvent(@RequestBody CalendarEventDto calendarEvent) {
        log.info("Creating calendar event {}", calendarEvent);
        String date = LocalDate.now().toString();
        DateTime startTime = new DateTime(date);
        String endDate = LocalDate.now().plusDays(1).toString();
        DateTime endTime = new DateTime(endDate);
        String email = "kapil.kumar.baser@gmail.com";

        try {
            googleCalendarService.createEvent(email, calendarEvent.title());
        } catch (IOException _) {
            log.warn("Error creating calendar event");
        }

        return "Successfully created calendar event";
    }
}
