package com.grillgauge.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.grillgauge.api.services.RegisterHubService;
import org.springframework.web.bind.annotation.RequestBody;

record RegisterHubResponse(Long hubId, Integer otp, String otpExpiresAt) {
}

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

    @PostMapping("")
    public RegisterHubResponse registerHub() {
        return new RegisterHubResponse(123L, 123, "yes");
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
