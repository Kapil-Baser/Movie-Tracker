package com.example.movieapi.controller;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.service.MovieSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
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

    private List<MovieDto> createMovieDtoList() {
        MovieDto projectHailMaryDto = new MovieDto(1L,
                "Project Hail Mary",
                "Project Hail Mary overview", Set.of("Adventure", "Science-Fiction"),
                "/8Tfys3mDZVp4tNoH2ktm06a0Tau.jpg", "/yihdXomYb5kTeSivtFndMy5iDmf.jpg",
                null, "2026-03-15", "157", "Project Hail Mary tagline",
                "tt12042730", null);

        MovieDto Scream7Dto = new MovieDto(2L,
                "Scream 7",
                "Scream 7 overview", Set.of("Horror", "Crime"),
                "/hz7TdCrpLLt2Dz7S3PS2HG9rpAO.jpg", "/jjyuk0edLiW8vOSnlfwWCCLpbh5.jpg",
                "2026-03-31", "2026-02-25", "114", "Scream 7 tagline",
                "tt27047903", null);

        return List.of(projectHailMaryDto, Scream7Dto);
    }

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

    @Test
    void syncMostAnticipated_shouldSyncMostAnticipated() throws Exception {

        List<MovieDto> movieDtoList = createMovieDtoList();

        when(movieSyncService.syncMostAnticipated())
                .thenReturn(movieDtoList);

        mockMvc.perform(get("/api/v1/admin/movie/anticipated").with(user("admin").roles("ADMIN")))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].title").value("Project Hail Mary"))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].title").value("Scream 7"))
                .andExpect(status().isOk());

        verify(movieSyncService, times(1)).syncMostAnticipated();
    }

    @Test
    void syncNowPlayingFromTmdb_returnsNoContentWhenSyncingIsNotPossible() throws Exception {
        when(movieSyncService.syncNowPlayingMoviesFromTmdb(1)).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/admin/movie/now-playing/{page_no}", 1).with(csrf())
                .with(user("admin")
                        .roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(movieSyncService, times(1)).syncNowPlayingMoviesFromTmdb(1);
    }

    @Test
    void syncNowPlayingFromTmdb_returnsCreatedWhenSyncingIsPossible() throws Exception {

        List<MovieDto> movieDtoList = createMovieDtoList();

        when(movieSyncService.syncNowPlayingMoviesFromTmdb(1)).thenReturn(movieDtoList);

        mockMvc.perform(post("/api/v1/admin/movie/now-playing/{page_no}", 1)
                .with(csrf())
                .with(user("admin").roles("ADMIN")))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].title").value("Project Hail Mary"))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].title").value("Scream 7"))
                .andExpect(status().isCreated());

        verify(movieSyncService, times(1)).syncNowPlayingMoviesFromTmdb(1);
    }

    @Test
    void trendingMoviesFromTrakt_returnsNoContentWhenAlreadySynced() throws Exception {
        when(movieSyncService.syncTrendingMoviesFromTrakt()).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/admin/movie/trending")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(movieSyncService, times(1)).syncTrendingMoviesFromTrakt();
    }

    @Test
    void trendingMoviesFromTrakt_returnsCreatedWhenPossible() throws Exception {

        List<MovieDto> movieDtoList = createMovieDtoList();

        when(movieSyncService.syncTrendingMoviesFromTrakt()).thenReturn(movieDtoList);

        mockMvc.perform(post("/api/v1/admin/movie/trending")
                .with(csrf())
                .with(user("admin").roles("ADMIN")))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].title").value("Project Hail Mary"))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].title").value("Scream 7"))
                .andExpect(status().isCreated());

        verify(movieSyncService, times(1)).syncTrendingMoviesFromTrakt();
    }

}