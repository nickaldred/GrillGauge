package com.grillgauge.api.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.grillgauge.api.controllers.RegisterHubController.HubRegistrationRequest;
import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.repositorys.HubRepository;

@Service
public class RegisterHubService {

    private final HubRepository hubRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegisterHubService(final HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

    public record HubRegistrationResponse(
            Long hubId,
            String otp,
            Instant otpExpiresAt) {
    }

    public HubRegistrationResponse registerHub(HubRegistrationRequest request) {
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(300); // 5 minutes
        Hub hub = new Hub(otp, expiresAt, buildMetadata(request)); // Save Hub as PENDING
        Hub savedHub = hubRepository.save(hub);

        return new HubRegistrationResponse(
                savedHub.getId(),
                otp,
                expiresAt);
    }

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
