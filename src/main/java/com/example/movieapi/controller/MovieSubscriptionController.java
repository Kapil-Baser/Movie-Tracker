package com.example.movieapi.controller;

import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.MovieSubscriptionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user/subscribe")
public class MovieSubscriptionController {

    private final MovieSubscriptionService subscriptionService;

    public MovieSubscriptionController(MovieSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/toggle")
    public String toggleSubscription(@RequestParam Long movieId,
                                     @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                     Model model) {
        boolean isSubscribed = subscriptionService.toggleSubscription(authenticatedUser, movieId);

        model.addAttribute("movieId", movieId);
        model.addAttribute("isSubscribed", isSubscribed);

        return "fragments/buttons :: subscribe-to-movie";
    }
}
