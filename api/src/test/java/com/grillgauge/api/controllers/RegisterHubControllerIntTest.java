package com.grillgauge.api.controllers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.grillgauge.api.controllers.RegisterHubController.HubConfirmRequest;
import com.grillgauge.api.controllers.RegisterHubController.HubRegistrationRequest;
import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;
import com.grillgauge.api.services.RegisterHubService.HubRegistrationResponse;
import com.grillgauge.api.utils.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegisterHubControllerIntTest {

    private static final String REGISTER_URL = "/api/v1/register/register";
    private static final String CONFIRM_URL = "/api/v1/register/confirm";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HubRepository hubRepository;

    @BeforeEach
    public void setup() {
        testUtils.clearDatabase();
    }

    /**
     * Helper method to register a hub.
     * 
     * @param model     The hub model.
     * @param fwVersion The firmware version.
     * @return The HubRegistrationResponse containing hub ID, OTP, and expiration
     *         time.
     */
    private HubRegistrationResponse registerHub(final String model, final String fwVersion) {
        HubRegistrationRequest registerRequest = new HubRegistrationRequest("ModelY", "2.0.0");
        ResponseEntity<HubRegistrationResponse> registerResponse = restTemplate.postForEntity(REGISTER_URL,
                registerRequest, HubRegistrationResponse.class);
        assertTrue(registerResponse.getStatusCode().is2xxSuccessful());
        HubRegistrationResponse registrationBody = registerResponse.getBody();
        assertNotNull(registrationBody);
        return registrationBody;
    }

    /**
     * Create a test user.
     * 
     * @param email     The user's email.
     * @param firstName The user's first name.
     * @param lastName  The user's last name.
     * @return The created User entity.
     */
    private User createUser(final String email, final String firstName, final String lastName) {
        User testUser = new User(email, firstName, lastName);
        return userRepository.save(testUser);
    }

    @Test
    public void testRegisterHubSuccess() {
        // Given
        HubRegistrationRequest request = new HubRegistrationRequest(
                "ModelX", "1.0.0");
        // When
        ResponseEntity<HubRegistrationResponse> response = restTemplate.postForEntity(REGISTER_URL, request,
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
        // Given
        User testUser = createUser("nickaldred@hotmail.co.uk", "Nick", "Aldred");
        HubRegistrationResponse hubRegistrationResponse = registerHub(REGISTER_URL, REGISTER_URL);

        // When
        HubConfirmRequest confirmRequest = new HubConfirmRequest(
                hubRegistrationResponse.hubId(), hubRegistrationResponse.otp(), testUser.getEmail());
        ResponseEntity<Void> confirmResponse = restTemplate.postForEntity(CONFIRM_URL,
                confirmRequest,
                Void.class);
        // Then
        assertTrue(confirmResponse.getStatusCode().is2xxSuccessful());
        Long hubId = hubRegistrationResponse.hubId();
        assertNotNull(hubId);
        Hub confirmedHub = hubRepository.findById(hubId).orElse(null);
        assertNotNull(confirmedHub);
        assertEquals(testUser.getEmail(), confirmedHub.getOwner().getEmail());
        assertEquals(Hub.HubStatus.CONFIRMED, confirmedHub.getStatus());
        assertNull(confirmedHub.getOtp());
        assertNull(confirmedHub.getOtpExpiresAt());
        assertNull(confirmedHub.getOtpHash());
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

    @Test
    public void testConfirmHubInvalidOTP() {
        // Given
        User testUser = createUser("nickaldred@hotmail.co.uk", "Nick", "Aldred");
        HubRegistrationResponse hubRegistrationResponse = registerHub(REGISTER_URL, REGISTER_URL);

        // When
        HubConfirmRequest confirmRequest = new HubConfirmRequest(
                hubRegistrationResponse.hubId(), "bad-otp", testUser.getEmail());
        ResponseEntity<Void> confirmResponse = restTemplate.postForEntity(CONFIRM_URL,
                confirmRequest,
                Void.class);
        // Then
        assertTrue(confirmResponse.getStatusCode().is5xxServerError());
        Long hubId = hubRegistrationResponse.hubId();
        assertNotNull(hubId);
        Hub confirmedHub = hubRepository.findById(hubId).orElse(null);
        assertNotNull(confirmedHub);
        assertNull(confirmedHub.getOwner());
        assertEquals(Hub.HubStatus.PENDING, confirmedHub.getStatus());
        assertNotNull(confirmedHub.getOtp());
        assertNotNull(confirmedHub.getOtpExpiresAt());
    }
}
