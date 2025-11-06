package com.example.movieapi.utility;

public class ReleaseTypeUtil {

    // TMDB Release Type Constants
    public static final int PREMIERE = 1;
    public static final int THEATRICAL_LIMITED = 2;
    public static final int THEATRICAL = 3;
    public static final int DIGITAL = 4;
    public static final int PHYSICAL = 5;
    public static final int TV = 6;

    public static String getReleaseTypeName(int type) {
        switch (type) {
            case PREMIERE -> {
                return "Premiere";
            }
            case THEATRICAL_LIMITED -> {
                return "Theatrical (Limited)";
            }
            case THEATRICAL -> {
                return "Theatrical";
            }
            case DIGITAL -> {
                return "Digital";
            }
            case PHYSICAL -> {
                return "Physical";
            }
            case TV -> {
                return "TV";
            }
            default -> {
                return "Unknown";
            }
        }
    }

    public static boolean isTheatrical(int type) {
        return type == THEATRICAL || type == THEATRICAL_LIMITED || type == PREMIERE;
    }
}
