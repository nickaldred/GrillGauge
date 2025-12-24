package com.grillgauge.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;
import com.grillgauge.api.utils.TestUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HubControllerIntTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private HubRepository hubRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TestUtils testUtils;

  private User testUser;
  private Hub testHub;

  @BeforeEach
  void setUp() {
    testUtils.clearDatabase();
    testUser = new User("nick@hotmail.co.uk", "Nick", "Bloggs");
    testUser = userRepository.save(testUser);
    Hub hub = new Hub(testUser, "Test Hub");
    testHub = hubRepository.save(hub);
  }

  @Test
  void testStoreHubSuccessful() throws Exception {
    // When
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/hub")
                    .with(
                  jwt()
                    .jwt(jwt -> jwt.subject(testUser.getEmail()))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testHub)))
            .andExpect(status().isCreated())
            .andReturn();

    Hub storedHub = objectMapper.readValue(result.getResponse().getContentAsString(), Hub.class);

    // Then
    assertNotNull(storedHub.getId());
    assertEquals(testHub.getName(), storedHub.getName());
    assertEquals(testUser.getEmail(), storedHub.getOwner().getEmail());
    Hub fetchedHub = hubRepository.findById(storedHub.getId()).orElse(null);
    assertNotNull(fetchedHub);
    assertEquals(storedHub.getId(), fetchedHub.getId());
    assertEquals(storedHub.getName(), fetchedHub.getName());
    assertEquals(storedHub.getOwner().getEmail(), fetchedHub.getOwner().getEmail());
  }
}
