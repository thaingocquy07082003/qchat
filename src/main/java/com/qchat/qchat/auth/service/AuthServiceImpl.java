package com.qchat.qchat.auth.service;

import com.qchat.qchat.auth.dto.request.LoginRequest;
import com.qchat.qchat.auth.dto.request.RegisterRequest;
import com.qchat.qchat.auth.dto.request.TokenRefreshRequest;
import com.qchat.qchat.auth.dto.response.AuthResponse;
import com.qchat.qchat.auth.entity.RefreshToken;
import com.qchat.qchat.auth.entity.User;
import com.qchat.qchat.auth.entity.UserProfile;
import com.qchat.qchat.auth.enums.OAuthProvider;
import com.qchat.qchat.auth.repository.RefreshTokenRepository;
import com.qchat.qchat.auth.repository.UserRepository;
import com.qchat.qchat.config.JwtProperties;
import com.qchat.qchat.exception.AppException;
import com.qchat.qchat.exception.ErrorCode;
import com.qchat.qchat.security.CustomUserDetails;
import com.qchat.qchat.security.JwtService;
import com.qchat.qchat.security.oauth2.GoogleTokenVerifier;
import com.qchat.qchat.security.oauth2.GoogleTokenVerifier.GoogleIdTokenClaims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtService             jwtService;
    private final JwtProperties          jwtProperties;
    private final StringRedisTemplate    redisTemplate;
    private final GoogleTokenVerifier    googleTokenVerifier;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    // ── Register ────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() == null && request.getPhoneNumber() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST_DATA);
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AppException(ErrorCode.PHONE_NUMBER_EXISTED);
        }

        UserProfile profile = UserProfile.builder()
                .displayName(request.getDisplayName())
                .build();

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .profile(profile)
                .build();

        profile.setUser(user);
        User saved = userRepository.save(user);

        log.info("New user registered: {}", saved.getUsername());
        return buildTokenPair(saved, null);
    }

    // ── Login ───────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo) {
        User user = resolveUser(request.getIdentity());

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.LOGIN_FAIL);
        }

        if (!user.isActive()) {
            throw new AppException(ErrorCode.DENIED_PERMISSION);
        }

        log.info("User logged in: {}", user.getUsername());
        return buildTokenPair(user, deviceInfo);
    }

    // ── Refresh ─────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse refresh(TokenRefreshRequest request) {
        String hash = jwtService.hashToken(request.getRefreshToken());

        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndIsRevokedFalse(hash)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        // Token rotation: revoke old, issue new pair
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildTokenPair(stored.getUser(), stored.getDeviceInfo());
    }

    // ── Logout ──────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String rawRefreshToken, String rawAccessToken) {
        if (rawRefreshToken != null) {
            String hash = jwtService.hashToken(rawRefreshToken);
            refreshTokenRepository.findByTokenHashAndIsRevokedFalse(hash)
                    .ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
        }

        // Blacklist the access token in Redis until it expires
        if (rawAccessToken != null && jwtService.validateAccessToken(rawAccessToken)) {
            long remainingMs = jwtService.getRemainingMs(rawAccessToken);
            if (remainingMs > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + rawAccessToken,
                        "1",
                        remainingMs,
                        TimeUnit.MILLISECONDS);
            }
        }
    }

    // ── Google OAuth2 (mobile) ───────────────────────────────────

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(String idToken, String deviceInfo) {
        GoogleIdTokenClaims claims = googleTokenVerifier.verify(idToken);

        User user = userRepository
                .findByOauthProviderAndOauthId(OAuthProvider.GOOGLE, claims.googleId())
                .map(existing -> updateGoogleUser(existing, claims))
                .orElseGet(() -> registerGoogleUser(claims));

        log.info("Google login for user: {}", user.getUsername());
        return buildTokenPair(user, deviceInfo);
    }

    private User registerGoogleUser(GoogleIdTokenClaims claims) {
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);

        UserProfile profile = UserProfile.builder()
                .displayName(claims.name() != null ? claims.name() : username)
                .avatarUrl(claims.pictureUrl())
                .build();

        User user = User.builder()
                .username(username)
                .email(claims.email())
                .oauthProvider(OAuthProvider.GOOGLE)
                .oauthId(claims.googleId())
                .isEmailVerified(claims.emailVerified())
                .profile(profile)
                .build();

        profile.setUser(user);
        log.info("Registering new Google user: {}", username);
        return userRepository.save(user);
    }

    private User updateGoogleUser(User existing, GoogleIdTokenClaims claims) {
        UserProfile profile = existing.getProfile();
        if (profile != null && claims.pictureUrl() != null) {
            profile.setAvatarUrl(claims.pictureUrl());
        }
        return userRepository.save(existing);
    }

    // ── Private helpers ─────────────────────────────────────────

    private User resolveUser(String identity) {
        return userRepository.findByEmail(identity)
                .or(() -> userRepository.findByUsername(identity))
                .or(() -> userRepository.findByPhoneNumber(identity))
                .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAIL));
    }

    private AuthResponse buildTokenPair(User user, String deviceInfo) {
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken     = jwtService.generateAccessToken(userDetails);
        String rawRefreshToken = jwtService.generateRefreshTokenRaw();
        String hashedToken     = jwtService.hashToken(rawRefreshToken);

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hashedToken)
                .expiresAt(OffsetDateTime.now()
                        .plusSeconds(jwtProperties.getRefreshTokenExpirationMs() / 1000))
                .deviceInfo(deviceInfo)
                .build());

        UserProfile profile = user.getProfile();
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .accessTokenExpiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .displayName(profile != null ? profile.getDisplayName() : null)
                        .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                        .build())
                .build();
    }
}
