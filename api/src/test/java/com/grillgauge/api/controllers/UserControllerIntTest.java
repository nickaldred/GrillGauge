package com.grillgauge.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.UserRepository;
import com.grillgauge.api.utils.TestUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
class UserControllerIntTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TestUtils testUtils;

  @BeforeEach
  public void setup() {
    testUtils.clearDatabase();
  }

  @Test
  void storeUserSuccessful() throws Exception {
    // Given
    User user = new User("nick@hotmail.co.uk", "Nick", "Bloggs");

    // When
    mockMvc
        .perform(
            post("/api/v1/user")
                .with(
                    jwt()
                        .jwt(
                            jwt -> {
                              jwt.subject(user.getEmail());
                              jwt.claim("roles", List.of("USER"));
                            }))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
        .andExpect(status().isCreated());

    // Then
    List<User> users = userRepository.findAll();
    assertEquals(1, users.size());
    User savedUser = users.get(0);
    assertEquals(user.getEmail(), savedUser.getEmail());
    assertEquals(user.getFirstName(), savedUser.getFirstName());
    assertEquals(user.getLastName(), savedUser.getLastName());
  }
}
