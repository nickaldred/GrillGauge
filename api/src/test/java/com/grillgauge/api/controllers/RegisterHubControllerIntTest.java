package com.grillgauge.api.controllers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.grillgauge.api.controllers.RegisterHubController.HubRegistrationRequest;
import com.grillgauge.api.services.RegisterHubService.HubRegistrationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegisterHubControllerIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testRegisterHub() {
        // Given
        String url = "/api/v1/register/register";
        HubRegistrationRequest request = new HubRegistrationRequest(
                "ModelX", "1.0.0");
        // When
        ResponseEntity<HubRegistrationResponse> response = restTemplate.postForEntity(url, request,
                HubRegistrationResponse.class);
        // Then
        assertTrue(response.getStatusCode().is2xxSuccessful());
        HubRegistrationResponse hubRegistrationResponse = response.getBody();
        assertNotNull(hubRegistrationResponse);

        assertNotNull(hubRegistrationResponse.hubId());
        assertNotNull(hubRegistrationResponse.otp());
        assertNotNull(hubRegistrationResponse.otpExpiresAt());
    }

    @Test
    public void testRegisterHubWithNullRequest() {
        // Given
        String url = "/api/v1/register/register";
        // When
        ResponseEntity<HubRegistrationResponse> response = restTemplate.postForEntity(url, null,
                HubRegistrationResponse.class);
        // Then
        assertTrue(response.getStatusCode().is4xxClientError());
    }
}
