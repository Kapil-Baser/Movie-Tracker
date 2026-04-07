package com.example.movieapi.utility;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtil {
    private static final String UNKNOWN = "Unknown";

    private FormatUtil() {}

    public static String formatRuntime(int runtime) {
        Duration duration = Duration.ofMinutes(runtime);
        return String.format("%dh %dmin", duration.toHours(), duration.toMinutesPart());
    }

    public static String formatReleaseDate(LocalDate releaseDate) {
        if (releaseDate != null) {
            return releaseDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH));
        }
        return UNKNOWN;
    }

    public static LocalDate parseReleaseDate(String formattedDate) {
        if (formattedDate != null && !formattedDate.equals(UNKNOWN)) {
            return LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH));
        }
        return null;
    }

    public static String formatUserRegisterDate(LocalDateTime userRegisterDate) {
        if (userRegisterDate != null) {
            return userRegisterDate.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
        }
        return UNKNOWN;
    }
}
