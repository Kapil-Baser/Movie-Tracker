package com.example.movieapi.utility;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtil {

    private FormatUtil() {}

    public static String formatRuntime(int runtime) {
        Duration duration = Duration.ofMinutes(runtime);
        return String.format("%dh %dmin", duration.toHours(), duration.toMinutesPart());
    }

    public static String formatReleaseDate(LocalDate releaseDate) {
        if (releaseDate != null) {
            return releaseDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH));
        }
        return "Unknown";
    }

    public static LocalDate parseReleaseDate(String formattedDate) {
        if (formattedDate != null && !formattedDate.equals("Unknown")) {
            return LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH));
        }
        return null;
    }
}
