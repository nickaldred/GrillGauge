package com.grillgauge.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HubControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HubRepository hubRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Hub testHub;

    @BeforeEach
    void setUp() {
        testUser = new User("nick@hotmail.co.uk", "Nick", "Bloggs");
        testUser = userRepository.save(testUser);
        Hub hub = new Hub(testUser, "apiKey", "Test Hub");
        testHub = hubRepository.save(hub);
    }

    @Test
    void testStoreHubSuccessful() throws Exception {
        // When
        MvcResult result = mockMvc.perform(post("/api/v1/hub")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testHub)))
                .andExpect(status().isCreated()).andReturn();

        Hub storedHub = objectMapper.readValue(result.getResponse().getContentAsString(), Hub.class);

        // Then
        assertNotNull(storedHub.getId());
        assertEquals(testHub.getName(), storedHub.getName());
        assertEquals(testHub.getApiKey(), storedHub.getApiKey());
        assertEquals(testUser.getId(), storedHub.getOwner().getId());
        Hub fetchedHub = hubRepository.findById(storedHub.getId()).orElse(null);
        assertNotNull(fetchedHub);
        assertEquals(storedHub.getId(), fetchedHub.getId());
        assertEquals(storedHub.getName(), fetchedHub.getName());
        assertEquals(storedHub.getApiKey(), fetchedHub.getApiKey());
        assertEquals(storedHub.getOwner().getId(), fetchedHub.getOwner().getId());
    }
}
