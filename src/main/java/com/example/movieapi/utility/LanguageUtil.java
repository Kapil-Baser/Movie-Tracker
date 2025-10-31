package com.example.movieapi.utility;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class LanguageUtil {
    private static final Logger logger = LoggerFactory.getLogger(LanguageUtil.class);

    private LanguageUtil() {}

    public static String getLanguageName(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            return "Unknown";
        }

        try {
            Locale locale = Locale.of(languageCode);
            String displayName = locale.getDisplayName(Locale.ENGLISH);

            /* Writing it here so I can remember that if displayName comes out same
                as the language code then that means that it is unrecognized.
            */
            return displayName.isEmpty() ? languageCode.toUpperCase() : displayName;
        } catch (Exception e) {
            logger.warn("Failed to get language name for code: {}", languageCode, e);
            return  languageCode.toUpperCase();
        }
    }
}
