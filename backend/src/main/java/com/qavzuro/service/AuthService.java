package com.qavzuro.service;

import com.qavzuro.domain.NotificationType;
import com.qavzuro.domain.RefreshToken;
import com.qavzuro.domain.User;
import com.qavzuro.dto.request.LoginRequest;
import com.qavzuro.dto.request.RegisterRequest;
import com.qavzuro.dto.response.AuthResponse;
import com.qavzuro.dto.response.UserResponse;
import com.qavzuro.exception.AuthenticationFailedException;
import com.qavzuro.exception.ConflictException;
import com.qavzuro.repository.RefreshTokenRepository;
import com.qavzuro.repository.UserRepository;
import com.qavzuro.security.JwtService;
import com.qavzuro.security.TokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000; // 15 min

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    /** Local carrier for a freshly-minted refresh token: the persisted (hashed) record plus the one-time raw value. */
    private record IssuedRefreshToken(RefreshToken record, String rawToken) {}

    public AuthResponse register(RegisterRequest req, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmail(req.getEmail().toLowerCase())) {
            throw new ConflictException("An account with this email already exists.");
        }
        User user = User.builder()
                .email(req.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .phone(req.getPhone())
                .roleCodes(new HashSet<>(Set.of("CUSTOMER")))
                .enabled(true)
                .build();
        user = userRepository.save(user);

        auditService.record(user.getId(), user.getEmail(), "REGISTER", "USER", user.getId(), null);
        notificationService.notify(user.getId(), NotificationType.ACCOUNT_SECURITY,
                "Welcome to Qavzuro", "Your account has been created successfully.", null);

        String familyId = UUID.randomUUID().toString();
        IssuedRefreshToken issued = newRefreshToken(user.getId(), familyId, httpRequest);
        return buildAuthResponse(user, issued);
    }

    public AuthResponse login(LoginRequest req, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password."));

        if (user.isAccountLocked() && user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new AuthenticationFailedException("Account temporarily locked due to repeated failed attempts. Try again later.");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockedUntil(Instant.now().plusMillis(LOCK_DURATION_MS));
            }
            userRepository.save(user);
            auditService.record(user.getId(), user.getEmail(), "LOGIN_FAILED", "USER", user.getId(), null);
            throw new AuthenticationFailedException("Invalid email or password.");
        }

        if (!user.isEnabled()) {
            throw new AuthenticationFailedException("This account has been disabled.");
        }

        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockedUntil(null);
        userRepository.save(user);

        auditService.record(user.getId(), user.getEmail(), "LOGIN", "USER", user.getId(), null);

        String familyId = UUID.randomUUID().toString();
        IssuedRefreshToken issued = newRefreshToken(user.getId(), familyId, httpRequest);
        return buildAuthResponse(user, issued);
    }

    public AuthResponse refresh(String presentedRefreshToken, HttpServletRequest httpRequest) {
        String hash = TokenHashUtil.sha256(presentedRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid refresh token."));

        if (stored.isRevoked()) {
            // Reuse of an already-rotated/revoked token: possible theft. Revoke the whole family.
            refreshTokenRepository.findByFamilyId(stored.getFamilyId())
                    .forEach(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
            throw new AuthenticationFailedException("Refresh token reuse detected. All sessions revoked - please log in again.");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthenticationFailedException("Refresh token expired. Please log in again.");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AuthenticationFailedException("User no longer exists."));

        // Rotate: revoke the presented token, issue a brand-new one in the same family.
        IssuedRefreshToken issued = newRefreshToken(user.getId(), stored.getFamilyId(), httpRequest);
        stored.setRevoked(true);
        stored.setReplacedByTokenHash(issued.record().getTokenHash());
        refreshTokenRepository.save(stored);

        return buildAuthResponse(user, issued);
    }

    public void logout(String presentedRefreshToken) {
        if (presentedRefreshToken == null || presentedRefreshToken.isBlank()) return;
        String hash = TokenHashUtil.sha256(presentedRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    public void logoutAllSessions(String userId) {
        refreshTokenRepository.findByUserIdAndRevokedFalse(userId)
                .forEach(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
    }

    private IssuedRefreshToken newRefreshToken(String userId, String familyId, HttpServletRequest httpRequest) {
        String rawToken = jwtService.generateOpaqueRefreshToken();
        RefreshToken token = RefreshToken.builder()
                .tokenHash(TokenHashUtil.sha256(rawToken))
                .userId(userId)
                .familyId(familyId)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
                .revoked(false)
                .userAgent(httpRequest != null ? httpRequest.getHeader("User-Agent") : null)
                .ipAddress(httpRequest != null ? httpRequest.getRemoteAddr() : null)
                .build();
        RefreshToken saved = refreshTokenRepository.save(token);
        return new IssuedRefreshToken(saved, rawToken);
    }

    private AuthResponse buildAuthResponse(User user, IssuedRefreshToken issued) {
        Set<String> permissions = authorizationService.resolvePermissions(user);
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), permissions);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(issued != null ? issued.rawToken() : null)
                .accessTokenExpiresInMs(jwtService.getAccessTokenExpirationMs())
                .user(toUserResponse(user))
                .permissions(permissions)
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .roleCodes(user.getRoleCodes())
                .addresses(user.getAddresses())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
