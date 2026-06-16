package com.qchat.qchat.auth.controller;

import com.qchat.qchat.auth.dto.request.GoogleLoginRequest;
import com.qchat.qchat.auth.dto.request.LoginRequest;
import com.qchat.qchat.auth.dto.request.RegisterRequest;
import com.qchat.qchat.auth.dto.request.TokenRefreshRequest;
import com.qchat.qchat.auth.dto.response.AuthResponse;
import com.qchat.qchat.auth.service.AuthService;
import com.qchat.qchat.common.ApiResponse;
import com.qchat.qchat.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/register
     * Body: { username, email?, phoneNumber?, password, displayName }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    /**
     * POST /api/v1/auth/login
     * Body: { identity (email|username|phone), password }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String deviceInfo = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.login(request, deviceInfo);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * POST /api/v1/auth/google
     * Body: { idToken }  — idToken comes from Flutter's google_sign_in SDK
     */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletRequest httpRequest) {

        String deviceInfo = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.loginWithGoogle(request.getIdToken(), deviceInfo);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * POST /api/v1/auth/refresh
     * Body: { refreshToken }
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {

        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    /**
     * POST /api/v1/auth/logout
     * Header: Authorization: Bearer <accessToken>
     * Body: { refreshToken }
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletRequest httpRequest) {

        String accessToken = extractBearerToken(httpRequest);
        authService.logout(request.getRefreshToken(), accessToken);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }

    /**
     * GET /api/v1/auth/me
     * Returns currently authenticated user info from JWT principal.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomUserDetails>> me(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(userDetails));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
