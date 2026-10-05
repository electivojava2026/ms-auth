package com.eventpass.ms_auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eventpass.ms_auth.dto.AuthResponse;
import com.eventpass.ms_auth.dto.LoginRequest;
import com.eventpass.ms_auth.model.Role;
import com.eventpass.ms_auth.model.User;
import com.eventpass.ms_auth.security.JwtService;
import com.eventpass.ms_auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Autowired
        private JwtService jwtService;

        @MockitoBean
        private AuthService authService;

        @Test
        void shouldReturnTokenOnSuccessfulLogin() throws Exception {
                AuthResponse response = AuthResponse.builder()
                                .token("mock-jwt-token")
                                .type("Bearer")
                                .id(1L)
                                .name("Admin")
                                .email("admin@eventpass.com")
                                .role(Role.STAFF)
                                .build();

                when(authService.login(any(LoginRequest.class))).thenReturn(response);

                LoginRequest loginRequest = new LoginRequest("admin@eventpass.com", "admin123");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                                .andExpect(jsonPath("$.type").value("Bearer"))
                                .andExpect(jsonPath("$.email").value("admin@eventpass.com"))
                                .andExpect(jsonPath("$.role").value("STAFF"))
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Admin"));
        }

        @Test
        void shouldReturn401WhenInvalidCredentials() throws Exception {
                when(authService.login(any(LoginRequest.class)))
                                .thenThrow(new org.springframework.security.authentication.BadCredentialsException(
                                                "Invalid email or password"));

                LoginRequest loginRequest = new LoginRequest("admin@eventpass.com", "wrong");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("Unauthorized"))
                                .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }

        @Test
        void shouldRejectSecuredEndpointWithoutToken() throws Exception {
                mockMvc.perform(get("/api/v1/users"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void shouldAllowSecuredEndpointWithValidJwtToken() throws Exception {
                User user = new User(1L, "Admin User", "admin@eventpass.com", "admin123", Role.STAFF);
                String token = jwtService.generateToken(user);

                mockMvc.perform(get("/api/v1/users")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isNoContent()); // Because users list is empty in this test mock
        }
}
