package com.grillgauge.api.controllers;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.ProbeRepository;
import com.grillgauge.api.domain.repositorys.ReadingRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;
import com.grillgauge.api.utils.TestUtils;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class ProbeControllerIntTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ReadingRepository readingRepository;

  @Autowired
  private ProbeRepository probeRepository;

  @Autowired
  private HubRepository hubRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestUtils testUtils;

  private List<Reading> readings;
  private User testUser;
  private Hub testHub;
  private Probe testProbe;

  @BeforeEach
  void setUp() {
    testUtils.clearDatabase();
    testUser = new User("nick@hotmail.co.uk", "Nick", "Bloggs");
    testUser = userRepository.save(testUser);
    testHub = new Hub(testUser, "Test Hub");
    hubRepository.save(testHub);
    testProbe = new Probe(1, testHub, testUser, (float) 200, "probe 1");
    probeRepository.save(testProbe);

    Reading reading1 = new Reading(testProbe, (float) 180, Instant.parse("2024-01-01T10:00:00Z"));
    Reading reading2 = new Reading(testProbe, (float) 180, Instant.parse("2024-01-01T11:00:00Z"));
    Reading reading3 = new Reading(testProbe, (float) 180, Instant.parse("2024-01-01T12:00:00Z"));
    Reading reading4 = new Reading(testProbe, (float) 180, Instant.parse("2024-01-01T12:30:00Z"));

    readings = List.of(reading1, reading2, reading3, reading4);
    readingRepository.saveAll(readings);
  }

  @Test
  void testGetReadingsForProbeBetween() throws Exception {
    // Given
    String start = "2024-01-01T10:30:00Z";
    String end = "2024-01-01T12:00:00Z";
    String probeKey = testProbe.getId().toString();

    // When / Then
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/probe/readings/between")
                .param("probeIds", probeKey)
                .param("start", start)
                .param("end", end))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
                "$.['" + probeKey + "'].length()")
                .value(2))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
                "$.['" + probeKey + "'][0].timestamp")
                .value("2024-01-01T11:00:00Z"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
                "$.['" + probeKey + "'][1].timestamp")
                .value("2024-01-01T12:00:00Z"));
  }
}
