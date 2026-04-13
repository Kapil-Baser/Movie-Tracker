package com.example.movieapi.controller;

import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.MoviesRepository;
import com.example.movieapi.service.MovieSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
@RequestMapping("/user/subscribe")
public class MovieSubscriptionController {

    private final MovieSubscriptionService subscriptionService;
    private final MoviesRepository moviesRepository;

    @Autowired
    public MovieSubscriptionController(MovieSubscriptionService subscriptionService, MoviesRepository moviesRepository) {
        this.subscriptionService = subscriptionService;
        this.moviesRepository = moviesRepository;
    }

    /*@PostMapping("/toggle")
    public String toggleSubscription(@RequestParam Long movieId,
                                     @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                     Model model) {
        boolean isSubscribed = subscriptionService.toggleSubscription(authenticatedUser, movieId);

        model.addAttribute("movieId", movieId);
        model.addAttribute("isSubscribed", isSubscribed);

        return "fragments/buttons :: subscribe-to-movie";
    }*/

    @PostMapping("/toggle")
    public FragmentsRendering toggleSubscription(@RequestParam Long movieId,
                                                 @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                 Model model) {

        boolean isSubscribed = subscriptionService.toggleSubscription(authenticatedUser, movieId);
        String movieTitle = moviesRepository.findTitleByMovieId(movieId);

        if (isSubscribed) {
            model.addAttribute("message", "You will be notified when '" + movieTitle + "' is out for streaming.");
        } else {
            model.addAttribute("message", "Unsubscribed from being notified when '" + movieTitle + "' is out for streaming.");
        }

        model.addAttribute("isSubscribed", isSubscribed);
        model.addAttribute("movieId", movieId);


        return FragmentsRendering
                .fragment("fragments/buttons :: subscribe-to-movie")
                .fragment("fragments/toasts :: hx-success")
                .build();

    }
}
