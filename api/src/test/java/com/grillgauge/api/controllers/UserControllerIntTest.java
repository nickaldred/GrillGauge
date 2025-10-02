package com.grillgauge.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void storeUserSuccessful() throws Exception {
        // Given
        User user = new User("nick@hotmail.co.uk", "Nick", "Bloggs", "1234");

        // When
        mockMvc.perform(post("/api/v1/user")
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
        assertEquals(user.getPasswordHash(), savedUser.getPasswordHash());
    }

}
