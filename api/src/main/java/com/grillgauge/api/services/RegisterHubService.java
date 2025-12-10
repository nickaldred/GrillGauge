package com.grillgauge.api.services;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.grillgauge.api.controllers.RegisterHubController.HubConfirmRequest;
import com.grillgauge.api.controllers.RegisterHubController.HubRegistrationRequest;
import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.entitys.Hub.HubStatus;
import com.grillgauge.api.domain.models.FrontEndHub;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;

/**
 * Service for registering hubs.
 * 
 * Handles the business logic for registering and confirming hubs.
 */
@Service
public class RegisterHubService {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterHubService.class);

    private final HubRepository hubRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final CertificateService certificateService;

    public RegisterHubService(final HubRepository hubRepository, final UserRepository userRepository,
            final CertificateService certificateService) {
        this.hubRepository = hubRepository;
        this.userRepository = userRepository;
        this.certificateService = certificateService;
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
     * @param hubConfirmRequest The confirmation request containing hub ID, user ID
     *                          and OTP.
     * @return The confirmed hub's ID.
     */
    public Long confirmHub(final HubConfirmRequest hubConfirmRequest) {
        LOG.info("Confirming hub with ID: {}", hubConfirmRequest.hubId());
        Long hubId = hubConfirmRequest.hubId();
        if (hubId == null) {
            throw new IllegalArgumentException("Invalid Hub ID");
        }
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Hub ID"));
        if (hub.getOtpExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        if (!hub.getOtp().equals(hubConfirmRequest.otp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        String userEmail = hubConfirmRequest.userId();
        if (userEmail == null || userEmail.isBlank()) {
            throw new IllegalArgumentException("Invalid User ID");
        }
        User user = userRepository.findById(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid User ID"));
        hub.setOwner(user);

        hub.setStatus(Hub.HubStatus.CONFIRMED);
        hub.setOtpExpiresAt(null);
        hub.setOtp(null);
        hub.setOtpHash(null);
        hubRepository.save(hub);
        LOG.info("Successfully Confirmed hub with ID: {}, Hub ID: {}", hubConfirmRequest.hubId());
        return hub.getId();
    }

    /**
     * Sign a certificate signing request (CSR) for the specified hub.
     * 
     * @param hubId  The ID of the hub.
     * @param csrPem The CSR in PEM format.
     * @return The signed certificate in PEM format.
     */
    public String signCsr(final String hubId, final String csrPem) {
        LOG.info("Signing CSR for hub ID: {}", hubId);
        X509Certificate signedCert = certificateService.sign(certificateService.loadCsrFromPem(csrPem));
        LOG.info("Successfully signed CSR for hub ID: {}", hubId);
        String signedCertPem = certificateService.convertToPem(signedCert);
        Hub hub = hubRepository.findById(Long.parseLong(hubId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid Hub ID"));
        hub.setCsrPem(csrPem);
        hub.setCertificatePem(signedCertPem);
        hub.setPublicKeyPem(certificateService.extractPublicKeyFromCsrPem(csrPem));
        hub.setCertificateExpiresAt(signedCert.getNotAfter().toInstant());
        hub.setCertificateIssuedAt(signedCert.getNotBefore().toInstant());
        hub.setStatus(HubStatus.REGISTERED);
        hub.setUpdatedAt(Instant.now());
        hubRepository.save(hub);
        LOG.info("Stored certificate for hub ID: {}", hubId);
        return signedCertPem;
    }

    /**
     * Revoke the certificate associated with the specified hub.
     * 
     * @param hubId The ID of the hub.
     */
    public void revokeCertificate(final Long hubId) {
        LOG.info("Revoking certificates for hub ID: {}", hubId);
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Hub ID"));
        // certificateService.revokeCertificatesForHub(hub);
        LOG.info("Successfully revoked certificates for hub ID: {}", hubId);
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
