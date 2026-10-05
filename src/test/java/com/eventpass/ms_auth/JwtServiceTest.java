package com.eventpass.ms_auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.eventpass.ms_auth.model.Role;
import com.eventpass.ms_auth.model.User;
import com.eventpass.ms_auth.security.JwtService;

import io.jsonwebtoken.Claims;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
    }

    @Test
    void shouldGenerateAndExtractTokenWithRoleAndUserDetails() {
        User user = new User(1L, "Test User", "test@eventpass.com", "password", Role.STAFF);

        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));

        Claims claims = jwtService.extractAllClaims(token);
        assertEquals("test@eventpass.com", claims.getSubject());
        assertEquals(1L, ((Number) claims.get("id")).longValue());
        assertEquals("Test User", claims.get("name"));
        assertEquals("test@eventpass.com", claims.get("email"));
        assertEquals("STAFF", claims.get("role"));

        assertEquals("test@eventpass.com", jwtService.extractEmail(token));
        assertEquals("STAFF", jwtService.extractRole(token));
        assertEquals(1L, jwtService.extractUserId(token));
    }
}
