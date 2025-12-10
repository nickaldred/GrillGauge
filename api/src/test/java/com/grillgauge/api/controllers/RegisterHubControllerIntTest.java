package com.grillgauge.api.controllers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import com.grillgauge.api.controllers.RegisterHubController.HubRegistrationRequest;
import com.grillgauge.api.services.RegisterHubService.HubRegistrationResponse;
import com.grillgauge.api.utils.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegisterHubControllerIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestUtils testUtils;

    @BeforeEach
    public void setup() {
        testUtils.clearDatabase();
    }

    @Test
    public void testRegisterHubSuccess() {
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

    @Test
    public void testConfirmHubSuccess() {
        // // Given

        // // First register a hub to get a valid OTP
        // String registerUrl = "/api/v1/register/register";
        // HubRegistrationRequest registerRequest = new HubRegistrationRequest(
        // "ModelY", "2.0.0");
        // ResponseEntity<HubRegistrationResponse> registerResponse =
        // restTemplate.postForEntity(registerUrl,
        // registerRequest, HubRegistrationResponse.class);
        // assertTrue(registerResponse.getStatusCode().is2xxSuccessful());
        // HubRegistrationResponse registrationBody = registerResponse.getBody();
        // assertNotNull(registrationBody);

        // // Add user to confirm hub

        // // Now confirm the hub
        // String confirmUrl = "/api/v1/register/confirm";
        // RegisterHubController.HubConfirmRequest confirmRequest = new
        // RegisterHubController.HubConfirmRequest(
        // registrationBody.hubId(), null, registrationBody.otp());
        // // When
        // ResponseEntity<Void> confirmResponse = restTemplate.postForEntity(confirmUrl,
        // confirmRequest,
        // Void.class);
        // // Then
        // assertTrue(confirmResponse.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testConfirmHubWithNullRequest() {
        // Given
        String url = "/api/v1/register/confirm";
        // When
        ResponseEntity<Void> response = restTemplate.postForEntity(url, null,
                Void.class);
        // Then
        assertTrue(response.getStatusCode().is4xxClientError());
    }
}
