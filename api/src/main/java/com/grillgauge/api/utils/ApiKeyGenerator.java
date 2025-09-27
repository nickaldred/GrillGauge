package com.grillgauge.api.utils;

import org.springframework.stereotype.Component;

import com.grillgauge.api.domain.exceptions.ApiKeyGeneratorException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating and handling API keys.
 */
@Component
public class ApiKeyGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    /**
     * Generate a secure random key.
     * 
     * @return A base64 string of 32 random bytes
     */
    public String generateRandomKey() {
        byte[] buffer = new byte[32];
        secureRandom.nextBytes(buffer);
        return encoder.encodeToString(buffer);
    }

    /**
     * Hash the key with SHA-256 and salt with hubId.
     * 
     * @param key   The random part of the key
     * @param hubId The hubId to salt with
     * @return The hashed key as a base64 string
     */
    public String hashKey(String key, Long hubId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedKey = key + hubId;
            byte[] hashedBytes = digest.digest(saltedKey.getBytes(StandardCharsets.UTF_8));
            return encoder.encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new ApiKeyGeneratorException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Build the full API key by combining hubId and random part.
     * 
     * @param randomKey The random part of the key
     * @param hubId     The hubId
     * @return The full API key in the format {hubId}-{randomKey}
     */
    public String buildFullApiKey(String randomKey, Long hubId) {
        return hubId + "-" + randomKey;
    }

    /**
     * Extract the hubId from the full API key.
     * 
     * @param fullKey The full API key in the format {hubId}-{randomKey}
     * @return The extracted hubId
     */
    public Long extractHubId(String fullKey) {
        return Long.parseLong(fullKey.split("-", 2)[0]);
    }

    /**
     * Extract the random part from the full API key.
     * 
     * @param fullKey The full API key in the format {hubId}-{randomKey}
     * @return The extracted random part
     */
    public String extractRandomPart(String fullKey) {
        return fullKey.split("-", 2)[1];
    }
}
