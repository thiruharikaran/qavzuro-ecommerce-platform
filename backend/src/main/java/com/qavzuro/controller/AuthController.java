package com.qavzuro.controller;

import com.qavzuro.dto.request.LoginRequest;
import com.qavzuro.dto.request.RefreshRequest;
import com.qavzuro.dto.request.RegisterRequest;
import com.qavzuro.dto.response.AuthResponse;
import com.qavzuro.service.AuthService;
import com.qavzuro.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.register(req, httpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(req, httpRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refresh(req.getRefreshToken(), httpRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll() {
        authService.logoutAllSessions(currentUserService.getUserId());
        return ResponseEntity.noContent().build();
    }
}
