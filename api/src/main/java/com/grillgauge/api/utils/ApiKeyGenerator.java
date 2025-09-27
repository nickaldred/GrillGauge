package com.grillgauge.api.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ApiKeyGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    // Generate random key part
    public String generateRandomKey() {
        byte[] buffer = new byte[32];
        secureRandom.nextBytes(buffer);
        return encoder.encodeToString(buffer);
    }

    // Hash using hubId as salt
    public String hashKey(String key, Long hubId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedKey = key + hubId;
            byte[] hashedBytes = digest.digest(saltedKey.getBytes(StandardCharsets.UTF_8));
            return encoder.encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // Build the full API key to give to client (hubId-prefix)
    public String buildFullApiKey(String randomKey, Long hubId) {
        return hubId + "-" + randomKey;
    }

    // Extract hubId and random part from API key
    public Long extractHubId(String fullKey) {
        return Long.parseLong(fullKey.split("-", 2)[0]);
    }

    public String extractRandomPart(String fullKey) {
        return fullKey.split("-", 2)[1];
    }
}
