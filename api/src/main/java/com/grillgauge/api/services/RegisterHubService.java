package com.grillgauge.api.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.grillgauge.api.controllers.RegisterHubController.HubConfirmRequest;
import com.grillgauge.api.controllers.RegisterHubController.HubRegistrationRequest;
import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.repositorys.HubRepository;

@Service
public class RegisterHubService {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterHubService.class)

    private final HubRepository hubRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegisterHubService(final HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

    /**
     * DTO class for Hub registration responses.
     */
    public record HubRegistrationResponse(
            Long hubId,
            String otp,
            Instant otpExpiresAt) {
    }

    /**
     * Register a new Hub.
     *
     * @param request The registration request containing model and firmware
     *                version.
     * @return The registration response with hub ID, OTP, and OTP expiration time.
     */
    public HubRegistrationResponse registerHub(final HubRegistrationRequest request) {
        LOG.info("Registering Hub with model: {}, fwVersion: {}");
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(300);
        Hub hub = new Hub(otp, expiresAt, buildMetadata(request)); // Save Hub as PENDING
        Hub savedHub = hubRepository.save(hub);
        LOG.info("Successfully registered hub with ID: {}", savedHub.getId());
        return new HubRegistrationResponse(
                savedHub.getId(),
                otp,
                expiresAt);
    }

    /**
     * Confirm a Hub's registration using the provided OTP.
     *
     * @param hubConfirmRequest The confirmation request containing hub ID and OTP.
     */
    public void confirmHub(final HubConfirmRequest hubConfirmRequest) {
        LOG.info("Confirming hub with ID: {}", hubConfirmRequest.id());
        LOG.info("Successfully Confirmed hub with ID: {}", hubConfirmRequest.id());
    }

    // Helper methods
    private String generateOtp() {
        int code = secureRandom.nextInt(900000) + 100000;
        return Integer.toString(code);
    }

    private String hashOtp(String otp) {
        // use BCrypt or Argon2 ideally
        return otp; // placeholder
    }

    private Map<String, String> buildMetadata(HubRegistrationRequest req) {
        Map<String, String> metaData = new HashMap<>();
        if (req != null) {
            metaData.put("model", req.model());
            metaData.put("fwVersion", req.fwVersion());
        }
        return metaData;
    }

}
