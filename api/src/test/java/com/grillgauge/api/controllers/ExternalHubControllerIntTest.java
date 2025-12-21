// package com.grillgauge.api.controllers;

// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import
// org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.MvcResult;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.grillgauge.api.domain.entitys.ApiKey;
// import com.grillgauge.api.domain.entitys.Hub;
// import com.grillgauge.api.domain.entitys.Probe;
// import com.grillgauge.api.domain.entitys.Reading;
// import com.grillgauge.api.domain.entitys.User;
// import com.grillgauge.api.domain.models.HubCurrentState;
// import com.grillgauge.api.domain.models.HubReading;
// import com.grillgauge.api.domain.models.ProbeReading;
// import com.grillgauge.api.domain.repositorys.ApiKeyRepository;
// import com.grillgauge.api.domain.repositorys.HubRepository;
// import com.grillgauge.api.domain.repositorys.ProbeRepository;
// import com.grillgauge.api.domain.repositorys.ReadingRepository;
// import com.grillgauge.api.domain.repositorys.UserRepository;
// import com.grillgauge.api.utils.ApiKeyGenerator;

// import jakarta.transaction.Transactional;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest
// @AutoConfigureMockMvc
// @Transactional
// class ExternalHubControllerIntTest {

// @Autowired
// private MockMvc mockMvc;

// @Autowired
// private ProbeRepository probeRepository;

// @Autowired
// private ReadingRepository readingRepository;

// @Autowired
// private HubRepository hubRepository;

// @Autowired
// private UserRepository userRepository;

// @Autowired
// private ObjectMapper objectMapper;

// private User testUser;

// @BeforeEach
// void setUp() {
// testUser = new User("nick@hotmail.co.uk", "nick", "aldred");
// userRepository.save(testUser);
// }

// @Test
// void testStoreReadingSuccessful() throws Exception {
// // Given
// final Hub hub = new Hub(testUser, "Smoke Gauge");
// hubRepository.save(hub);
// final ProbeReading probeReading1 = new ProbeReading(1, (float) 120.23);
// final ProbeReading probeReading2 = new ProbeReading(2, (float) 180.23);
// final List<ProbeReading> probeReadings = List.of(probeReading1,
// probeReading2);
// final HubReading hubReading = new HubReading((long) 1234, probeReadings);
// final Probe probe1 = new Probe(1, hub, testUser, (float) 200, "probe 1");
// final Probe probe2 = new Probe(2, hub, testUser, (float) 160, "probe 2");
// probeRepository.save(probe1);
// probeRepository.save(probe2);

// final String fullApiKey = generateAndStoreApiKey((long) hub.getId());

// // When
// mockMvc.perform(post("/api/v1/externalHub")
// .header("X-API-KEY", fullApiKey)
// .contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(hubReading)))
// .andExpect(status().isCreated());

// // Then
// List<Reading> readingsProbe1 =
// readingRepository.findByProbeId(probe1.getId());
// List<Reading> readingsProbe2 =
// readingRepository.findByProbeId(probe2.getId());
// assertEquals(1, readingsProbe1.size());
// assertEquals(probeReading1.getCurrentTemp(),
// readingsProbe1.get(0).getCurrentTemp());
// assertEquals(1, readingsProbe2.size());
// assertEquals(probeReading2.getCurrentTemp(),
// readingsProbe2.get(0).getCurrentTemp());
// }

// @Test
// void testStoreReadingUnsuccessful() throws Exception {
// // Given
// final ProbeReading probeReading1 = new ProbeReading(1, (float) 120.23);
// final List<ProbeReading> probeReadings = List.of(probeReading1);
// final HubReading hubReading = new HubReading((long) 1234, probeReadings);
// final Hub hub = new Hub(testUser, "apikey1", "Smoke Gauge");
// hubRepository.save(hub);

// final String fullApiKey = generateAndStoreApiKey((long) hub.getId());

// // When and Then
// mockMvc.perform(post("/api/v1/externalHub")
// .contentType(MediaType.APPLICATION_JSON)
// .header("X-API-KEY", fullApiKey)
// .content(objectMapper.writeValueAsString(hubReading)))
// .andExpect(status().isNotFound());
// }

// @Test
// void testStoreReadingUnauthorised() throws Exception {
// // Given
// final List<ProbeReading> probeReadings = List.of();
// final HubReading hubReading = new HubReading((long) 1234, probeReadings);

// // When and Then
// mockMvc.perform(post("/api/v1/externalHub")
// .contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(hubReading)))
// .andExpect(status().isUnauthorized());
// }

// @Test
// void testgetHubCurrentStateSuccessful() throws Exception {
// // Given
// Hub hub = new Hub(testUser, "apikey1", "Smoke Gauge");
// hubRepository.save(hub);
// Probe probe1 = new Probe(1, hub, testUser, (float) 200, "probe 1");
// Probe probe2 = new Probe(2, hub, testUser, (float) 160, "probe 2");
// List<Probe> probes = List.of(probe1, probe2);
// probeRepository.save(probe1);
// probeRepository.save(probe2);

// String fullApiKey = generateAndStoreApiKey(hub.getId());

// // When
// MvcResult result = mockMvc.perform(get("/api/v1/externalHub")
// .header("X-API-KEY", fullApiKey))
// .andExpect(status().isOk()).andReturn();

// // Then
// HubCurrentState hubCurrentState =
// objectMapper.readValue(result.getResponse().getContentAsString(),
// HubCurrentState.class);
// assertEquals(hub.getName(), hubCurrentState.getHubName());
// assertEquals(hub.getId(), hubCurrentState.getHubId());
// assertThat(hubCurrentState.getProbes())
// .usingRecursiveFieldByFieldElementComparator()
// .isEqualTo(probes);
// }

// @Test
// void testgetHubCurrentStateUnsuccessfulNoHub() throws Exception {
// // Given
// String fullApiKey = generateAndStoreApiKey((long) 2);

// // When
// mockMvc.perform(get("/api/v1/externalHub/%d".formatted((long) 1234))
// .header("X-API-KEY", fullApiKey))
// .andExpect(status().isNotFound());
// }

// @Test
// void testgetHubCurrentStateUnsuccessfulNoProbes() throws Exception {
// // Given
// Hub hub = new Hub(testUser, "apikey1", "Smoke Gauge");
// hub = hubRepository.save(hub);

// String fullApiKey = generateAndStoreApiKey((long) 2);

// // When
// mockMvc.perform(get("/api/v1/externalHub/%d".formatted(hub.getId()))
// .header("X-API-KEY", fullApiKey))
// .andExpect(status().isNotFound());
// }

// @Test
// void testgetHubCurrentStateUnauthorised() throws Exception {
// // Given
// Hub hub = new Hub(testUser, "apikey1", "Smoke Gauge");
// hub = hubRepository.save(hub);

// // When
// mockMvc.perform(get("/api/v1/externalHub/%d".formatted(hub.getId())))
// .andExpect(status().isUnauthorized());
// }
// }
