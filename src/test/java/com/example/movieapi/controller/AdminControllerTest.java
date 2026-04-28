package com.example.movieapi.controller;

import com.example.movieapi.service.MovieSyncService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieSyncService movieSyncService;

    @Test
    void getToken_returns401_whenNoUserIsProvided() throws Exception {
        RequestBuilder request = get("/api/v1/admin/movie");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getToken_returns200_whenUserIsProvided() throws Exception {
        mockMvc.perform(get("/api/v1/admin/movie")
                        .with(user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void syncReleaseDates_shouldSyncReleaseDates() throws Exception {
        mockMvc.perform(post("/api/v1/admin/movie/sync-release-dates")
                        .with(user("admin")
                                .roles("ADMIN"))
                        .with(csrf()))
                .andExpect(content().string("Synced release dates"))
                .andExpect(status().isOk());

        verify(movieSyncService, times(1)).fetchAndSyncDigitalReleaseDates();
    }

}