package com.eventpass.ms_auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eventpass.ms_auth.dto.AuthResponse;
import com.eventpass.ms_auth.dto.LoginRequest;
import com.eventpass.ms_auth.model.Role;
import com.eventpass.ms_auth.model.User;
import com.eventpass.ms_auth.repository.UserRepository;
import com.eventpass.ms_auth.security.JwtService;
import com.eventpass.ms_auth.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Admin EventPass", "admin@eventpass.com", passwordEncoder.encode("secret123"),
                Role.STAFF);
    }

    @Test
    void shouldLoginSuccessfullyWithEncodedPassword() {
        when(userRepository.findByEmail("admin@eventpass.com")).thenReturn(Optional.of(sampleUser));
        when(jwtService.generateToken(sampleUser)).thenReturn("fake-jwt-token");

        LoginRequest request = new LoginRequest("admin@eventpass.com", "secret123");
        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("admin@eventpass.com", response.getEmail());
        assertEquals("Admin EventPass", response.getName());
        assertEquals(Role.STAFF, response.getRole());
        assertEquals(1L, response.getId());
    }

    @Test
    void shouldLoginSuccessfullyWithPlainTextPasswordForLegacySeed() {
        User plainUser = new User(2L, "User", "user@eventpass.com", "user123", Role.USER);
        when(userRepository.findByEmail("user@eventpass.com")).thenReturn(Optional.of(plainUser));
        when(jwtService.generateToken(plainUser)).thenReturn("plain-user-jwt");

        LoginRequest request = new LoginRequest("user@eventpass.com", "user123");
        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("plain-user-jwt", response.getToken());
        assertEquals(Role.USER, response.getRole());
    }

    @Test
    void shouldThrowWhenPasswordDoesNotMatch() {
        when(userRepository.findByEmail("admin@eventpass.com")).thenReturn(Optional.of(sampleUser));

        LoginRequest request = new LoginRequest("admin@eventpass.com", "wrong-password");
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@eventpass.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown@eventpass.com", "password");
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
