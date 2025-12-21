package com.grillgauge.api.controllers;

import static org.junit.jupiter.api.Assertions.*;

import com.grillgauge.api.controllers.RegisterHubController.HubConfirmRequest;
import com.grillgauge.api.controllers.RegisterHubController.HubRegistrationRequest;
import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;
import com.grillgauge.api.services.RegisterHubService.HubRegistrationResponse;
import com.grillgauge.api.utils.TestUtils;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegisterHubControllerIntTest {

  private static final String REGISTER_URL = "/api/v1/register/register";
  private static final String CONFIRM_URL = "/api/v1/register/confirm";

  @Value("${otp.expiry.seconds}")
  private int otpExpirySeconds;

  @Value("${certificate.ca-cert}")
  private String caCertPath;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private TestUtils testUtils;

  @Autowired private UserRepository userRepository;

  @Autowired private HubRepository hubRepository;

  @Autowired private com.grillgauge.api.services.CertificateService certificateService;

  @BeforeEach
  public void setup() {
    testUtils.clearDatabase();
  }

  private HubRegistrationResponse registerHub(final String model, final String fwVersion) {
    HubRegistrationRequest registerRequest = new HubRegistrationRequest(model, fwVersion);
    ResponseEntity<HubRegistrationResponse> hubRegistrationResponse =
        restTemplate.postForEntity(REGISTER_URL, registerRequest, HubRegistrationResponse.class);
    assertTrue(hubRegistrationResponse.getStatusCode().is2xxSuccessful());
    HubRegistrationResponse registrationBody = hubRegistrationResponse.getBody();
    assertNotNull(registrationBody);
    return registrationBody;
  }

  private User createUser(final String email, final String firstName, final String lastName) {
    User testUser = new User(email, firstName, lastName);
    return userRepository.save(testUser);
  }

  private void confirmHub(final HubRegistrationResponse regResp, final String userEmail) {
    HubConfirmRequest confirmRequest =
        new HubConfirmRequest(regResp.hubId(), regResp.otp(), userEmail);
    ResponseEntity<Void> confirmResponse =
        restTemplate.postForEntity(CONFIRM_URL, confirmRequest, Void.class);
    assertTrue(confirmResponse.getStatusCode().is2xxSuccessful());
  }

  private String generateCsrPem(final Long hubId) throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair keyPair = kpg.generateKeyPair();
    X500Name subject = new X500Name("CN=hub-" + hubId);
    JcaPKCS10CertificationRequestBuilder p10Builder =
        new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
    ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
    PKCS10CertificationRequest csr = p10Builder.build(signer);
    StringWriter sw = new StringWriter();
    try (PemWriter pw = new PemWriter(sw)) {
      pw.writeObject(new PemObject("CERTIFICATE REQUEST", csr.getEncoded()));
    }
    return sw.toString();
  }

  private X509Certificate loadCertificateFromPem(final String pem) throws Exception {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    try (ByteArrayInputStream in = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8))) {
      return (X509Certificate) cf.generateCertificate(in);
    }
  }

  private X509Certificate loadCaCertificate() throws Exception {
    byte[] caBytes = Files.readAllBytes(Paths.get(caCertPath));
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    try (ByteArrayInputStream in2 = new ByteArrayInputStream(caBytes)) {
      return (X509Certificate) cf.generateCertificate(in2);
    }
  }

  private void setHubCertificateSerialFromCert(final Long hubId, final X509Certificate cert) {
    Hub hub = hubRepository.findById(hubId).orElseThrow();
    hub.setCertificateSerial(cert.getSerialNumber().longValue());
    hubRepository.save(hub);
  }

  private void assertCertRevoked(final X509Certificate signedCert) throws Exception {
    X509CRL crl = certificateService.revokeBySerial(signedCert.getSerialNumber(), new Date(), 5);
    List<X509CRL> crlList = List.of(crl);
    CertStoreParameters csParams = new CollectionCertStoreParameters(crlList);
    CertStore crlStore = CertStore.getInstance("Collection", csParams);

    X509Certificate caCert = loadCaCertificate();
    TrustAnchor ta = new TrustAnchor(caCert, null);
    Set<TrustAnchor> trustAnchors = Set.of(ta);
    PKIXParameters pkixParams = new PKIXParameters(trustAnchors);
    pkixParams.setRevocationEnabled(true);
    pkixParams.addCertStore(crlStore);

    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    CertPath certPath = cf.generateCertPath(List.of(signedCert));
    CertPathValidator validator = CertPathValidator.getInstance("PKIX");
    try {
      validator.validate(certPath, pkixParams);
      fail("Expected validation to fail for revoked certificate");
    } catch (CertPathValidatorException expected) {
      assertTrue(true); // Expected exception
    }
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
    ResponseEntity<HubRegistrationResponse> response =
        restTemplate.postForEntity(url, null, HubRegistrationResponse.class);
    // Then
    assertTrue(response.getStatusCode().is4xxClientError());
  }

  @Test
  public void testConfirmHubSuccess() {
    // Given
    User testUser = createUser("nickaldred@hotmail.co.uk", "Nick", "Aldred");
    HubRegistrationResponse hubRegistrationResponse = registerHub(REGISTER_URL, REGISTER_URL);

    // When
    HubConfirmRequest confirmRequest =
        new HubConfirmRequest(
            hubRegistrationResponse.hubId(), hubRegistrationResponse.otp(), testUser.getEmail());
    ResponseEntity<Void> confirmResponse =
        restTemplate.postForEntity(CONFIRM_URL, confirmRequest, Void.class);
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
    ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);
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
    unConfirmedHub.setOtpExpiresAt(
        unConfirmedHub.getOtpExpiresAt().minusSeconds(otpExpirySeconds + 1));
    hubRepository.save(unConfirmedHub);

    // When
    HubConfirmRequest confirmRequest =
        new HubConfirmRequest(
            hubRegistrationResponse.hubId(), hubRegistrationResponse.otp(), testUser.getEmail());
    ResponseEntity<Void> confirmResponse =
        restTemplate.postForEntity(CONFIRM_URL, confirmRequest, Void.class);

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
    HubConfirmRequest confirmRequest =
        new HubConfirmRequest(hubRegistrationResponse.hubId(), "bad-otp", testUser.getEmail());
    ResponseEntity<Void> confirmResponse =
        restTemplate.postForEntity(CONFIRM_URL, confirmRequest, Void.class);
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
    confirmHub(hubRegistrationResponse, testUser.getEmail());

    // Generate CSR
    String csrPem = generateCsrPem(hubId);

    // When
    String csrUrl = "/api/v1/register/" + hubId + "/csr";
    ResponseEntity<String> csrResponse = restTemplate.postForEntity(csrUrl, csrPem, String.class);

    // Then
    assertEquals(201, csrResponse.getStatusCode().value());
    String signedCertPem = csrResponse.getBody();
    assertNotNull(signedCertPem);
    assertTrue(signedCertPem.contains("BEGIN CERTIFICATE"));
    X509Certificate signedCert = loadCertificateFromPem(signedCertPem);
    X509Certificate caCert = loadCaCertificate();

    // Verify signature and issuer (compare DN components order-insensitively)
    signedCert.verify(caCert.getPublicKey());
    LdapName caLdap = new LdapName(caCert.getSubjectX500Principal().getName(X500Principal.RFC2253));
    LdapName issuerLdap =
        new LdapName(signedCert.getIssuerX500Principal().getName(X500Principal.RFC2253));
    Set<String> caRdns = caLdap.getRdns().stream().map(Rdn::toString).collect(Collectors.toSet());
    Set<String> issuerRdns =
        issuerLdap.getRdns().stream().map(Rdn::toString).collect(Collectors.toSet());
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
    String csrPem =
        "-----BEGIN CERTIFICATE REQUEST-----\n"
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
    String csrPem =
        "-----BEGIN CERTIFICATE REQUEST-----\n"
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

  @Test
  public void testRevokeCertificateSuccess() throws Exception {
    // Given
    User testUser = createUser("nickaldred@hotmail.co.uk", "Nick", "Aldred");
    HubRegistrationResponse hubRegistrationResponse = registerHub("ModelX", "1.0.0");
    Long hubId = hubRegistrationResponse.hubId();
    assertNotNull(hubId);

    confirmHub(hubRegistrationResponse, testUser.getEmail());
    String csrPem = generateCsrPem(hubId);

    // When - sign CSR
    String csrUrl = "/api/v1/register/" + hubId + "/csr";
    ResponseEntity<String> csrResponse = restTemplate.postForEntity(csrUrl, csrPem, String.class);
    assertEquals(201, csrResponse.getStatusCode().value());
    String signedCertPem = csrResponse.getBody();
    assertNotNull(signedCertPem);

    // Parse signed cert and set certificate serial on hub so revoke can find it
    X509Certificate signedCert = loadCertificateFromPem(signedCertPem);
    setHubCertificateSerialFromCert(hubId, signedCert);

    // When - revoke
    String revokeUrl = "/api/v1/register/" + hubId + "/revoke?reason=5";
    ResponseEntity<Void> revokeResponse = restTemplate.postForEntity(revokeUrl, null, Void.class);

    // Then
    assertTrue(revokeResponse.getStatusCode().is2xxSuccessful());
    // Validate that the certificate is revoked according to CA
    assertCertRevoked(signedCert);
    Hub updatedHub = hubRepository.findById(hubId).orElse(null);
    assertNotNull(updatedHub);
    assertNull(updatedHub.getCertificatePem());
    assertNull(updatedHub.getCsrPem());
    assertNull(updatedHub.getPublicKeyPem());
    assertNull(updatedHub.getCertificateIssuedAt());
    assertNull(updatedHub.getCertificateExpiresAt());
    assertEquals(Hub.HubStatus.REVOKED, updatedHub.getStatus());
  }

  @Test
  public void testRevokeCertificateForNonexistentHub() {
    // When
    String revokeUrl = "/api/v1/register/99999/revoke?reason=5";
    ResponseEntity<Void> revokeResponse = restTemplate.postForEntity(revokeUrl, null, Void.class);
    // Then
    assertEquals(500, revokeResponse.getStatusCode().value());
  }

  @Test
  public void testRevokeCertificateForHubWithoutCertificateSerial() {
    // Given
    HubRegistrationResponse hubRegistrationResponse = registerHub("ModelX", "1.0.0");
    Long hubId = hubRegistrationResponse.hubId();
    assertNotNull(hubId);
    // When
    String revokeUrl = "/api/v1/register/" + hubId + "/revoke?reason=5";
    ResponseEntity<Void> revokeResponse = restTemplate.postForEntity(revokeUrl, null, Void.class);
    // Then
    assertEquals(500, revokeResponse.getStatusCode().value());
  }
}
