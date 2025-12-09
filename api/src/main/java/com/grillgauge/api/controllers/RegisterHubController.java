package com.grillgauge.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.grillgauge.api.services.RegisterHubService;
import com.grillgauge.api.services.RegisterHubService.HubRegistrationResponse;

import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller for registering hubs.
 * 
 * Provides endpoints for registering a new hub.
 */
@RestController
@RequestMapping("/api/v1/register")
public class RegisterHubController {

    private RegisterHubService registerHubService;

    public RegisterHubController(final RegisterHubService registerHubService) {
        this.registerHubService = registerHubService;

    }

    /**
     * DTO class for Hub registration requests.
     */
    public record HubRegistrationRequest(
            String model,
            String fwVersion) {
    }

    /**
     * DTO class for Hub registration confirmation.
     */
    public record HubConfirmRequest(
            Long id,
            String otp,
            String userId) {

    }

    /**
     * Register a new Hub.
     *
     * @param request The registration request containing model and firmware
     *                version.
     * @return The registration response with hub ID, OTP, and OTP expiration time.
     */
    @PostMapping("/register")
    public ResponseEntity<HubRegistrationResponse> registerHub(
            @RequestBody() HubRegistrationRequest request) {
        HubRegistrationResponse response = registerHubService.registerHub(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm a Hub's registration using the provided OTP.
     *
     * @param hubConfirm The confirmation request containing hub ID and OTP.
     */
    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.OK)
    public void confirmAuthenticated(
            final @RequestBody() HubConfirmRequest hubConfirm) {
        registerHubService.confirmHub(hubConfirm);
    }

    /**
     * Signs a certificate signing request (CSR) for the specified hub.
     *
     * @param hubId  The ID of the hub.
     * @param csrPem The CSR in PEM format.
     * @return The signed certificate in PEM format.
     */
    @PostMapping("/{hubId}/csr")
    @ResponseStatus(HttpStatus.CREATED)
    public String signCertificate(final @PathVariable String hubId, @RequestBody String csrPem) {
        return registerHubService.signCsr(hubId, csrPem);
    }

    /**
     * Revoke the certificate for the specified hub.
     *
     * @param hubId The ID of the hub.
     */
    @PostMapping("/{hubId}/revoke")
    @ResponseStatus(HttpStatus.OK)
    public void revokeCertificate(final @PathVariable Long hubId) {
        registerHubService.revokeCertificate(hubId);
    }

}
