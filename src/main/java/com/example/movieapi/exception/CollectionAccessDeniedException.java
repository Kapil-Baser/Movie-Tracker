package com.example.movieapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class CollectionAccessDeniedException extends RuntimeException {
    public CollectionAccessDeniedException(String message) {
        super(message);
    }
}
