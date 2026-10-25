package com.smartcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcare.dto.auth.LoginRequest;
import com.smartcare.dto.auth.RegisterPatientRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Module 27 - "Authentication tests" and "Controller tests", exercised
 * end-to-end through the real Spring Security filter chain against H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void register_thenLogin_thenAccessProtectedEndpoint() throws Exception {
        RegisterPatientRequest register = RegisterPatientRequest.builder()
                .fullName("Test Patient")
                .email("patient.it@example.com")
                .password("Password123")
                .build();

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_PATIENT"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(registerResponse).get("accessToken").asText();

        // Protected endpoint should reject an unauthenticated request...
        mockMvc.perform(get("/api/patients/me"))
                .andExpect(status().isUnauthorized());

        // ...and accept the same request once the JWT is attached.
        mockMvc.perform(get("/api/patients/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Test Patient"));
    }

    @Test
    void register_withDuplicateEmail_returnsConflict() throws Exception {
        RegisterPatientRequest register = RegisterPatientRequest.builder()
                .fullName("Dup User")
                .email("dup.it@example.com")
                .password("Password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        RegisterPatientRequest register = RegisterPatientRequest.builder()
                .fullName("Login Test")
                .email("login.it@example.com")
                .password("Password123")
                .build();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest badLogin = LoginRequest.builder().email("login.it@example.com").password("WrongPassword1").build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withInvalidPayload_returnsBadRequestWithFieldErrors() throws Exception {
        String invalidJson = "{\"fullName\":\"\",\"email\":\"not-an-email\",\"password\":\"123\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}
