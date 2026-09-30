package com.example.movieapi.exception;

public class ExternalMovieApiUnreachableException extends RuntimeException {
    public ExternalMovieApiUnreachableException(String message) {
        super(message);
    }
}
