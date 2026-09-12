package com.qavzuro.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Issues and validates short-lived JWT access tokens. Refresh tokens are
 * NOT JWTs - they are opaque random strings stored (hashed) server-side in
 * RefreshTokenRepository, which is what allows real revocation. This is
 * deliberate: a self-contained JWT cannot be revoked before it expires.
 */
@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey signingKey;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.signingKey = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String userId, String email, Set<String> permissions) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + props.getAccessTokenExpirationMs());
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("permissions", permissions.stream().collect(Collectors.joining(",")))
                .issuer(props.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generateOpaqueRefreshToken() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public long getAccessTokenExpirationMs() {
        return props.getAccessTokenExpirationMs();
    }

    public long getRefreshTokenExpirationMs() {
        return props.getRefreshTokenExpirationMs();
    }
}
