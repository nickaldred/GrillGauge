package com.grillgauge.api.controllers;

import java.util.Comparator;
import java.util.List;

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
import com.grillgauge.api.domain.models.HubCurrentState;
import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.domain.models.ProbeReading;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.ProbeRepository;
import com.grillgauge.api.domain.repositorys.ReadingRepository;

import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HubControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProbeRepository probeRepository;

    @Autowired
    private ReadingRepository readingRepository;

    @Autowired
    private HubRepository hubRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testStoreReadingSuccessful() throws Exception {
        // Given
        ProbeReading probeReading1 = new ProbeReading(1, (float) 120.23);
        ProbeReading probeReading2 = new ProbeReading(2, (float) 180.23);
        List<ProbeReading> probeReadings = List.of(probeReading1, probeReading2);
        HubReading hubReading = new HubReading((long) 1234, probeReadings);
        Probe probe1 = new Probe(1, (long) 1234, (float) 200, "probe 1");
        Probe probe2 = new Probe(2, (long) 1234, (float) 160, "probe 2");
        probeRepository.save(probe1);
        probeRepository.save(probe2);

        // When
        mockMvc.perform(post("/api/v1/hub")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hubReading)))
                .andExpect(status().isCreated());

        // Then
        List<Reading> readingsProbe1 = readingRepository.findByProbeId(probe1.getId());
        List<Reading> readingsProbe2 = readingRepository.findByProbeId(probe2.getId());
        assertEquals(1, readingsProbe1.size());
        assertEquals(probeReading1.getCurrentTemp(), readingsProbe1.get(0).getCurrentTemp());
        assertEquals(1, readingsProbe2.size());
        assertEquals(probeReading2.getCurrentTemp(), readingsProbe2.get(0).getCurrentTemp());
    }

    @Test
    void testStoreReadingUnsuccessful() throws Exception {
        // Given
        ProbeReading probeReading1 = new ProbeReading(1, (float) 120.23);
        List<ProbeReading> probeReadings = List.of(probeReading1);
        HubReading hubReading = new HubReading((long) 1234, probeReadings);

        // When and Then
        mockMvc.perform(post("/api/v1/hub")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hubReading)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testgetHubCurrentStateSuccessful() throws Exception {
        // Given
        Hub hub = new Hub((long) 1234, "apikey1", "Smoke Gauge");
        hub = hubRepository.save(hub);
        Probe probe1 = new Probe(1, hub.getId(), (float) 200, "probe 1");
        Probe probe2 = new Probe(2, hub.getId(), (float) 160, "probe 2");
        List<Probe> probes = List.of(probe1, probe2);
        probe1 = probeRepository.save(probe1);
        probe2 = probeRepository.save(probe2);

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/hub/%d".formatted(hub.getId())))
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
    void testgetHubCurrentStateUnsuccessfulNoHub() throws Exception {
        // When
        mockMvc.perform(get("/api/v1/hub/%d".formatted((long) 1234)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testgetHubCurrentStateUnsuccessfulNoProbes() throws Exception {
        // Given
        Hub hub = new Hub((long) 1234, "apikey1", "Smoke Gauge");
        hub = hubRepository.save(hub);

        // When
        mockMvc.perform(get("/api/v1/hub/%d".formatted(hub.getId())))
                .andExpect(status().isNotFound());
    }
}
