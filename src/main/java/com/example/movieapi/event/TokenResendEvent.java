package com.example.movieapi.event;

import com.example.movieapi.entity.AppUser;

public record TokenResendEvent(AppUser user) {
}
