package com.qavzuro.service;

import com.qavzuro.domain.RefreshToken;
import com.qavzuro.domain.User;
import com.qavzuro.dto.request.LoginRequest;
import com.qavzuro.dto.request.RegisterRequest;
import com.qavzuro.dto.response.AuthResponse;
import com.qavzuro.exception.AuthenticationFailedException;
import com.qavzuro.exception.ConflictException;
import com.qavzuro.repository.RefreshTokenRepository;
import com.qavzuro.repository.UserRepository;
import com.qavzuro.security.JwtProperties;
import com.qavzuro.security.JwtService;
import com.qavzuro.security.TokenHashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Covers: registration, login, BCrypt password hashing (never SHA-256 for
 * passwords), refresh-token rotation, and refresh-token reuse detection.
 */
class AuthServiceTest {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthorizationService authorizationService;
    private AuditService auditService;
    private NotificationService notificationService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = new BCryptPasswordEncoder(4); // low cost factor for fast tests
        authorizationService = mock(AuthorizationService.class);
        auditService = mock(AuditService.class);
        notificationService = mock(NotificationService.class);

        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-at-least-32-characters-long-1234");
        props.setAccessTokenExpirationMs(900_000);
        props.setRefreshTokenExpirationMs(1_209_600_000);
        props.setIssuer("qavzuro-test");
        jwtService = new JwtService(props);

        when(authorizationService.resolvePermissions(any())).thenReturn(Set.of("PRODUCT_VIEW"));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) u.setId("user-1");
            return u;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken t = inv.getArgument(0);
            if (t.getId() == null) t.setId("rt-1");
            return t;
        });

        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder, jwtService,
                authorizationService, auditService, notificationService);
    }

    @Test
    void register_hashesPasswordWithBCrypt_neverStoresPlaintext() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("New@Example.com");
        req.setPassword("SuperSecret1");
        req.setFirstName("Ada");
        req.setLastName("Lovelace");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        AuthResponse response = authService.register(req, null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertNotEquals("SuperSecret1", saved.getPasswordHash(), "Password must never be stored in plaintext");
        assertTrue(saved.getPasswordHash().startsWith("$2"), "Password hash should be a BCrypt hash");
        assertTrue(passwordEncoder.matches("SuperSecret1", saved.getPasswordHash()));
        assertEquals("new@example.com", saved.getEmail(), "Email should be normalized to lowercase");
        assertEquals(Set.of("CUSTOMER"), saved.getRoleCodes());
        assertNotNull(response.getAccessToken());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@example.com");
        req.setPassword("SuperSecret1");
        req.setFirstName("A");
        req.setLastName("B");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req, null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_succeedsWithCorrectPassword_andIssuesRefreshToken() {
        User user = User.builder().id("user-1").email("user@example.com")
                .passwordHash(passwordEncoder.encode("CorrectPass1"))
                .roleCodes(new HashSet<>(Set.of("CUSTOMER"))).enabled(true).failedLoginAttempts(0).build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("CorrectPass1");

        AuthResponse response = authService.login(req, null);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(0, user.getFailedLoginAttempts());
    }

    @Test
    void login_incrementsFailedAttempts_andLocksAfterThreshold() {
        User user = User.builder().id("user-1").email("user@example.com")
                .passwordHash(passwordEncoder.encode("CorrectPass1"))
                .roleCodes(new HashSet<>(Set.of("CUSTOMER"))).enabled(true).failedLoginAttempts(4).build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("WrongPassword");

        assertThrows(AuthenticationFailedException.class, () -> authService.login(req, null));
        assertEquals(5, user.getFailedLoginAttempts());
        assertTrue(user.isAccountLocked(), "Account should lock after reaching the max failed-attempt threshold");
    }

    @Test
    void login_rejectsWrongPassword_withoutRevealingWhichFieldWasWrong() {
        User user = User.builder().id("user-1").email("user@example.com")
                .passwordHash(passwordEncoder.encode("CorrectPass1"))
                .roleCodes(new HashSet<>()).enabled(true).build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrong");

        AuthenticationFailedException ex = assertThrows(AuthenticationFailedException.class, () -> authService.login(req, null));
        assertEquals("Invalid email or password.", ex.getMessage());
    }

    @Test
    void refresh_rotatesToken_revokingOldAndIssuingNew() {
        String rawToken = "raw-refresh-token-value";
        String hash = TokenHashUtil.sha256(rawToken);
        RefreshToken stored = RefreshToken.builder().id("rt-old").tokenHash(hash).userId("user-1")
                .familyId("family-1").expiresAt(Instant.now().plusSeconds(3600)).revoked(false).build();
        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(stored));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(
                User.builder().id("user-1").email("user@example.com").roleCodes(new HashSet<>()).enabled(true).build()));

        AuthResponse response = authService.refresh(rawToken, null);

        assertTrue(stored.isRevoked(), "The presented refresh token must be revoked after rotation");
        assertNotNull(stored.getReplacedByTokenHash());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(rawToken, response.getRefreshToken(), "Rotation must issue a brand-new refresh token");
    }

    @Test
    void refresh_withAlreadyRevokedToken_revokesEntireFamily_reuseDetection() {
        String rawToken = "stolen-refresh-token";
        String hash = TokenHashUtil.sha256(rawToken);
        RefreshToken stored = RefreshToken.builder().id("rt-1").tokenHash(hash).userId("user-1")
                .familyId("family-1").expiresAt(Instant.now().plusSeconds(3600)).revoked(true).build();
        RefreshToken sibling = RefreshToken.builder().id("rt-2").tokenHash("other-hash").userId("user-1")
                .familyId("family-1").expiresAt(Instant.now().plusSeconds(3600)).revoked(false).build();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.findByFamilyId("family-1")).thenReturn(java.util.List.of(stored, sibling));

        assertThrows(AuthenticationFailedException.class, () -> authService.refresh(rawToken, null));

        assertTrue(sibling.isRevoked(), "Reuse of a revoked refresh token must revoke the whole token family");
    }

    @Test
    void refresh_withExpiredToken_isRejected() {
        String rawToken = "expired-token";
        String hash = TokenHashUtil.sha256(rawToken);
        RefreshToken stored = RefreshToken.builder().id("rt-1").tokenHash(hash).userId("user-1")
                .familyId("family-1").expiresAt(Instant.now().minusSeconds(60)).revoked(false).build();
        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(stored));

        assertThrows(AuthenticationFailedException.class, () -> authService.refresh(rawToken, null));
    }
}
