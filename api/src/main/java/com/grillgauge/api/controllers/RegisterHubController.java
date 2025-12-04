package com.grillgauge.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.grillgauge.api.domain.models.HubRegistrationRequest;
import com.grillgauge.api.domain.models.HubRegistrationResponse;
import com.grillgauge.api.services.RegisterHubService;
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

    @PostMapping("/register")
    public ResponseEntity<HubRegistrationResponse> registerHub(
            @RequestBody(required = false) HubRegistrationRequest request) {
        HubRegistrationResponse response = registerHubService.registerHub(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.OK)
    public void confirmAuthenticated(final @RequestParam String hubId, @RequestParam Integer otp) {
    }

    @PostMapping("/{hubId}/csr")
    @ResponseStatus(HttpStatus.OK)
    public String signCertificate(final @PathVariable String hubId, @RequestBody String csrPem) {
        return csrPem;
    }

    @PostMapping("/{hubId}/revoke")
    @ResponseStatus(HttpStatus.OK)
    public void revokeCertificate(final @PathVariable String hubId) {
    }

}
