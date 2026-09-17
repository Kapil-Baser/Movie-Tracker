package com.example.movieapi.dto;

import java.io.Serializable;

public record RedirectInfo(String type, String message) implements Serializable {
    private static final long serialVersionID = 1L;
}
