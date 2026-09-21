package com.example.movieapi.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeWatchedOnDateDto {
    private LocalDate oldWatchedDate;
    private LocalDate newWatchedDate;
}
