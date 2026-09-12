package com.qavzuro.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * SHA-256 is used here ONLY as a checksum for looking up opaque refresh
 * tokens by their hash (an integrity/lookup use, not password hashing).
 * Passwords are hashed with BCrypt elsewhere - see SecurityConfig.
 */
public final class TokenHashUtil {
    private TokenHashUtil() {}

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
