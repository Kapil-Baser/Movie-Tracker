package com.example.movieapi.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class TokenHashUtil {

    private static final SecureRandom secureRandom = new SecureRandom();

    private TokenHashUtil() {}


    public static String generateRawToken() {
        return UUID.randomUUID() + "-" + Long.toHexString(secureRandom.nextLong());
    }

    public static String getHashedToken(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
