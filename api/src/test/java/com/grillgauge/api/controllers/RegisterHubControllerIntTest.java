package com.grillgauge.api.controllers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.convention.TestBean;

import com.grillgauge.api.controllers.RegisterHubController.HubConfirmRequest;
import com.grillgauge.api.controllers.RegisterHubController.HubRegistrationRequest;
import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;
import com.grillgauge.api.services.RegisterHubService.HubRegistrationResponse;
import com.grillgauge.api.utils.TestUtils;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import java.io.StringWriter;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import javax.security.auth.x500.X500Principal;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegisterHubControllerIntTest {

    private static final String REGISTER_URL = "/api/v1/register/register";
    private static final String CONFIRM_URL = "/api/v1/register/confirm";

    @Value("${otp.expiry.seconds}")
    private int otpExpirySeconds;

    @Value("${certificate.ca-cert}")
    private String caCertPath;

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
        HubRegistrationRequest registerRequest = new HubRegistrationRequest(model, fwVersion);
        ResponseEntity<HubRegistrationResponse> hubRegistrationResponse = restTemplate.postForEntity(REGISTER_URL,
                registerRequest, HubRegistrationResponse.class);
        assertTrue(hubRegistrationResponse.getStatusCode().is2xxSuccessful());
        HubRegistrationResponse registrationBody = hubRegistrationResponse.getBody();
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
        // When
        HubRegistrationResponse hubRegistrationResponse = registerHub("ModelX", "1.0.0");
        // Then
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
        Long hubId = hubRegistrationResponse.hubId();
        assertNotNull(hubId);
        Hub unConfirmedHub = hubRepository.findById(hubId).orElse(null);
        unConfirmedHub.setOtpExpiresAt(unConfirmedHub.getOtpExpiresAt().minusSeconds(otpExpirySeconds + 1));
        hubRepository.save(unConfirmedHub);

        // When
        HubConfirmRequest confirmRequest = new HubConfirmRequest(
                hubRegistrationResponse.hubId(), hubRegistrationResponse.otp(), testUser.getEmail());
        ResponseEntity<Void> confirmResponse = restTemplate.postForEntity(CONFIRM_URL,
                confirmRequest,
                Void.class);

        // Then
        assertTrue(confirmResponse.getStatusCode().is5xxServerError());
        assertNotNull(hubId);
        Hub confirmedHub = hubRepository.findById(hubId).orElse(null);
        assertNotNull(confirmedHub);
        assertNull(confirmedHub.getOwner());
        assertEquals(Hub.HubStatus.PENDING, confirmedHub.getStatus());
        assertNotNull(confirmedHub.getOtp());
        assertNotNull(confirmedHub.getOtpExpiresAt());
    }

    @Test
    public void testConfirmHubExpiredOTP() {
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

    @Test
    public void testSignCsrSuccess() throws Exception {
        // Given
        User testUser = createUser("nickaldred@hotmail.co.uk", "Nick", "Aldred");
        HubRegistrationResponse hubRegistrationResponse = registerHub("ModelX", "1.0.0");
        Long hubId = hubRegistrationResponse.hubId();
        assertNotNull(hubId);

        // Confirm the hub
        HubConfirmRequest confirmRequest = new HubConfirmRequest(
                hubRegistrationResponse.hubId(), hubRegistrationResponse.otp(), testUser.getEmail());
        ResponseEntity<Void> confirmResponse = restTemplate.postForEntity(CONFIRM_URL,
                confirmRequest,
                Void.class);
        assertTrue(confirmResponse.getStatusCode().is2xxSuccessful());

        // Generate a CSR (PKCS#10) using BouncyCastle
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();
        X500Name subject = new X500Name("CN=hub-" + hubId);
        JcaPKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(subject,
                keyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        PKCS10CertificationRequest csr = p10Builder.build(signer);
        StringWriter sw = new StringWriter();
        try (PemWriter pw = new PemWriter(sw)) {
            pw.writeObject(new PemObject("CERTIFICATE REQUEST", csr.getEncoded()));
        }
        String csrPem = sw.toString();

        // When
        String csrUrl = "/api/v1/register/" + hubId + "/csr";
        ResponseEntity<String> csrResponse = restTemplate.postForEntity(csrUrl, csrPem, String.class);

        // Then
        assertEquals(201, csrResponse.getStatusCode().value());
        String signedCertPem = csrResponse.getBody();
        assertNotNull(signedCertPem);
        assertTrue(signedCertPem.contains("BEGIN CERTIFICATE"));

        // Load signed cert and CA cert, verify signed cert is issued by CA
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate signedCert;
        try (ByteArrayInputStream in = new ByteArrayInputStream(signedCertPem.getBytes(StandardCharsets.UTF_8))) {
            signedCert = (X509Certificate) cf.generateCertificate(in);
        }
        byte[] caBytes = Files.readAllBytes(Paths.get(caCertPath));
        X509Certificate caCert;
        try (ByteArrayInputStream in2 = new ByteArrayInputStream(caBytes)) {
            caCert = (X509Certificate) cf.generateCertificate(in2);
        }

        // Verify signature and issuer (compare DN components order-insensitively)
        signedCert.verify(caCert.getPublicKey());
        LdapName caLdap = new LdapName(caCert.getSubjectX500Principal().getName(X500Principal.RFC2253));
        LdapName issuerLdap = new LdapName(signedCert.getIssuerX500Principal().getName(X500Principal.RFC2253));
        Set<String> caRdns = caLdap.getRdns().stream().map(Rdn::toString).collect(Collectors.toSet());
        Set<String> issuerRdns = issuerLdap.getRdns().stream().map(Rdn::toString).collect(Collectors.toSet());
        assertEquals(caRdns, issuerRdns);
        signedCert.checkValidity();

        Hub updatedHub = hubRepository.findById(hubId).orElse(null);
        assertNotNull(updatedHub);
        assertEquals(Hub.HubStatus.REGISTERED, updatedHub.getStatus());
        assertEquals(csrPem, updatedHub.getCsrPem());
        assertNotNull(updatedHub.getCertificatePem());
        assertEquals(signedCertPem, updatedHub.getCertificatePem());
        assertNotNull(updatedHub.getPublicKeyPem());
        assertNotNull(updatedHub.getCertificateIssuedAt());
        assertNotNull(updatedHub.getCertificateExpiresAt());
        assertEquals(testUser.getEmail(), updatedHub.getOwner().getEmail());
    }

    @Test
    public void testSignCsrForUnconfirmedHub() {
        // Given
        HubRegistrationResponse hubRegistrationResponse = registerHub("ModelX", "1.0.0");
        Long hubId = hubRegistrationResponse.hubId();
        assertNotNull(hubId);
        String csrPem = "-----BEGIN CERTIFICATE REQUEST-----\n"
                + "MIICWjCCAEMCAQAwTjELMAkGA1UEBhMCVVMxCzAJBgNVBAgMAkNBMQswCQYDVQQH\n"
                + "DAJMQTEPMA0GA1UECgwGQ29tcGFueTEPMA0GA1UEAwwGdGVzdC5jb20wggEiMA0G\n"
                + "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDFakeCSRl6YQ1Z6j0K3z3T8j6+X9O/\n"
                + "-----END CERTIFICATE REQUEST-----";

        // When
        String csrUrl = "/api/v1/register/" + hubId + "/csr";
        ResponseEntity<String> csrResponse = restTemplate.postForEntity(csrUrl, csrPem, String.class);
        // Then
        assertEquals(500, csrResponse.getStatusCode().value());
    }

    @Test
    public void testSignCsrWithNullBody() {
        // Given
        HubRegistrationResponse hubRegistrationResponse = registerHub("ModelX", "1.0.0");
        Long hubId = hubRegistrationResponse.hubId();
        assertNotNull(hubId);
        // When
        String csrUrl = "/api/v1/register/" + hubId + "/csr";
        ResponseEntity<String> csrResponse = restTemplate.postForEntity(csrUrl, null, String.class);
        // Then
        assertEquals(400, csrResponse.getStatusCode().value());
    }

    @Test
    public void testSignCsrForNonexistentHub() {
        // Given
        String csrPem = "-----BEGIN CERTIFICATE REQUEST-----\n"
                + "MIICWjCCAEMCAQAwTjELMAkGA1UEBhMCVVMxCzAJBgNVBAgMAkNBMQswCQYDVQQH\n"
                + "DAJMQTEPMA0GA1UECgwGQ29tcGFueTEPMA0GA1UEAwwGdGVzdC5jb20wggEiMA0G\n"
                + "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDFakeCSRl6YQ1Z6j0K3z3T8j6+X9O/\n"
                + "-----END CERTIFICATE REQUEST-----";
        // When
        String csrUrl = "/api/v1/register/99999/csr";
        ResponseEntity<String> csrResponse = restTemplate.postForEntity(csrUrl, csrPem, String.class);
        // Then
        assertEquals(500, csrResponse.getStatusCode().value());
    }
}
