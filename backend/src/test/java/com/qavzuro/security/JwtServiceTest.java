package com.qavzuro.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Covers JWT issuance/validation and confirms refresh tokens are opaque (not JWTs), so they can be revoked. */
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-at-least-32-characters-long-1234");
        props.setAccessTokenExpirationMs(900_000);
        props.setRefreshTokenExpirationMs(1_209_600_000);
        props.setIssuer("qavzuro-test");
        jwtService = new JwtService(props);
    }

    @Test
    void generatesValidAccessToken_withEmbeddedPermissions() {
        String token = jwtService.generateAccessToken("user-1", "user@example.com", Set.of("PRODUCT_VIEW", "ORDER_VIEW"));

        assertTrue(jwtService.isValid(token));
        Claims claims = jwtService.parseClaims(token);
        assertEquals("user-1", claims.getSubject());
        assertEquals("user@example.com", claims.get("email"));
        String perms = claims.get("permissions", String.class);
        assertTrue(perms.contains("PRODUCT_VIEW"));
        assertTrue(perms.contains("ORDER_VIEW"));
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.generateAccessToken("user-1", "user@example.com", Set.of());
        String tampered = token.substring(0, token.length() - 4) + "abcd";

        assertFalse(jwtService.isValid(tampered), "A token with a modified signature must not validate");
    }

    @Test
    void refreshTokensAreOpaque_notJwtFormat() {
        String refreshToken = jwtService.generateOpaqueRefreshToken();

        assertFalse(refreshToken.contains("."), "Refresh tokens must be opaque random values, not JWTs, so they can be revoked server-side");
        assertTrue(refreshToken.length() > 40, "Refresh token should have sufficient entropy");
    }

    @Test
    void generatesUniqueRefreshTokensOnEachCall() {
        String t1 = jwtService.generateOpaqueRefreshToken();
        String t2 = jwtService.generateOpaqueRefreshToken();
        assertNotEquals(t1, t2);
    }
}
