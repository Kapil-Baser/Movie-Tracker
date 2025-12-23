package com.example.movieapi.utility;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtil {

    public static String formatRuntime(int runtime) {
        Duration duration = Duration.ofMinutes(runtime);
        return String.format("%dh %dmin", duration.toHours(), duration.toMinutesPart());
    }

    public static String formatReleaseDate(LocalDate releaseDate) {
        return releaseDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH));
    }
}
