package com.grillgauge.api.controllers;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.models.HubCurrentState;
import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.domain.models.ProbeReading;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.ProbeRepository;
import com.grillgauge.api.domain.repositorys.ReadingRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;
import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExternalHubControllerIntTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ProbeRepository probeRepository;

  @Autowired
  private ReadingRepository readingRepository;

  @Autowired
  private HubRepository hubRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private User testUser;
  private X509Certificate cert;

  private X509Certificate loadTestCertificate() throws Exception {
    String certPath = "src/test/java/com/grillgauge/api/resources/certs/signed_cert.crt";
    byte[] certBytes = Files.readAllBytes(Paths.get(certPath));
    String pem = new String(certBytes, StandardCharsets.UTF_8);
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    try (var in = new java.io.ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8))) {
      return (X509Certificate) cf.generateCertificate(in);
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    cert = loadTestCertificate();
    testUser = new User("nick@hotmail.co.uk", "nick", "aldred");
    testUser = userRepository.save(testUser);
  }

  @Test
  void testStoreReadingSuccessful() throws Exception {
    // Given
    Hub hub = new Hub(testUser, "Smoke Gauge");
    hub = hubRepository.save(hub);
    final ProbeReading probeReading1 = new ProbeReading(1, (float) 120.23);
    final ProbeReading probeReading2 = new ProbeReading(2, (float) 180.23);
    final List<ProbeReading> probeReadings = List.of(probeReading1,
        probeReading2);
    final HubReading hubReading = new HubReading((long) 1234, probeReadings);
    Probe probe1 = new Probe(1, hub, testUser, (float) 200, "probe 1");
    Probe probe2 = new Probe(2, hub, testUser, (float) 160, "probe 2");
    probe1 = probeRepository.save(probe1);
    probe2 = probeRepository.save(probe2);
    hub.setCertificateSerial(cert.getSerialNumber().longValue());
    hub.setStatus(Hub.HubStatus.REGISTERED);
    hubRepository.save(hub);

    mockMvc.perform(post("/api/v1/externalHub")
        .with(x509(cert))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(hubReading)))
        .andExpect(status().isCreated());

    // Then
    List<Reading> readingsProbe1 = readingRepository.findByProbeId(probe1.getId());
    List<Reading> readingsProbe2 = readingRepository.findByProbeId(probe2.getId());
    assertEquals(1, readingsProbe1.size());
    assertEquals(probeReading1.getCurrentTemp(),
        readingsProbe1.get(0).getCurrentTemp());
    assertEquals(1, readingsProbe2.size());
    assertEquals(probeReading2.getCurrentTemp(),
        readingsProbe2.get(0).getCurrentTemp());
  }

  @Test
  void testStoreReadingUnsuccessful() throws Exception {
    // Given
    final ProbeReading probeReading1 = new ProbeReading(1, (float) 120.23);
    final List<ProbeReading> probeReadings = List.of(probeReading1);
    final HubReading hubReading = new HubReading((long) 1234, probeReadings);
    final Hub hub = new Hub(testUser, "Smoke Gauge");
    hubRepository.save(hub);
    hub.setCertificateSerial(cert.getSerialNumber().longValue());
    hub.setStatus(Hub.HubStatus.REGISTERED);
    hubRepository.save(hub);

    mockMvc.perform(post("/api/v1/externalHub")
        .with(x509(cert))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(hubReading)))
        .andExpect(status().isNotFound());
  }

  @Test
  void testStoreReadingForbidden() throws Exception {
    // Given
    final List<ProbeReading> probeReadings = List.of();
    final HubReading hubReading = new HubReading((long) 1234, probeReadings);

    // When and Then
    mockMvc.perform(post("/api/v1/externalHub")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(hubReading)))
        .andExpect(status().isForbidden());
  }

  @Test
  void testGetHubCurrentStateSuccessful() throws Exception {
    // Given
    Hub hub = new Hub(testUser, "Smoke Gauge");
    hubRepository.save(hub);
    Probe probe1 = new Probe(1, hub, testUser, (float) 200, "probe 1");
    Probe probe2 = new Probe(2, hub, testUser, (float) 160, "probe 2");
    List<Probe> probes = List.of(probe1, probe2);
    probeRepository.save(probe1);
    probeRepository.save(probe2);
    hub.setCertificateSerial(cert.getSerialNumber().longValue());
    hub.setStatus(Hub.HubStatus.REGISTERED);
    hubRepository.save(hub);

    // When
    MvcResult result = mockMvc.perform(get("/api/v1/externalHub")
        .with(x509(cert)))
        .andExpect(status().isOk()).andReturn();

    // Then
    HubCurrentState hubCurrentState = objectMapper.readValue(result.getResponse().getContentAsString(),
        HubCurrentState.class);
    assertEquals(hub.getName(), hubCurrentState.getHubName());
    assertEquals(hub.getId(), hubCurrentState.getHubId());
    assertThat(hubCurrentState.getProbes())
        .usingRecursiveFieldByFieldElementComparator()
        .isEqualTo(probes);
  }

  @Test
  void testGetHubCurrentStateUnsuccessfulNoHub() throws Exception {
    // When
    mockMvc.perform(get("/api/v1/externalHub")
        .with(x509(cert)))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetHubCurrentStateNoProbes() throws Exception {
    // Given
    Hub hub = new Hub(testUser, "Smoke Gauge");
    hub = hubRepository.save(hub);

    hub.setCertificateSerial(cert.getSerialNumber().longValue());
    hub.setStatus(Hub.HubStatus.REGISTERED);
    hubRepository.save(hub);

    // When & then
    mockMvc.perform(get("/api/v1/externalHub")
        .with(x509(cert)))
        .andExpect(status().isOk());
  }

  @Test
  void testGetHubCurrentStateForbidden() throws Exception {
    // Given
    Hub hub = new Hub(testUser, "Smoke Gauge");
    hub = hubRepository.save(hub);

    // When
    mockMvc.perform(get("/api/v1/externalHub"))
        .andExpect(status().isForbidden());
  }
}
