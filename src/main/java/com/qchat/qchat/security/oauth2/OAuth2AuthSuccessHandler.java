package com.qchat.qchat.security.oauth2;

import com.qchat.qchat.auth.entity.RefreshToken;
import com.qchat.qchat.auth.repository.RefreshTokenRepository;
import com.qchat.qchat.auth.repository.UserRepository;
import com.qchat.qchat.config.JwtProperties;
import com.qchat.qchat.exception.AppException;
import com.qchat.qchat.exception.ErrorCode;
import com.qchat.qchat.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String authorizedRedirectUri;

    private final JwtService              jwtService;
    private final JwtProperties           jwtProperties;
    private final UserRepository          userRepository;
    private final RefreshTokenRepository  refreshTokenRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken      = jwtService.generateAccessToken(oAuth2User);
        String rawRefreshToken  = jwtService.generateRefreshTokenRaw();
        String hashedToken      = jwtService.hashToken(rawRefreshToken);

        var user = userRepository.findById(oAuth2User.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hashedToken)
                .expiresAt(OffsetDateTime.now().plusSeconds(
                        jwtProperties.getRefreshTokenExpirationMs() / 1000))
                .build());

        clearAuthenticationAttributes(request, response);

        String redirectUri = HttpCookieOAuth2AuthorizationRequestRepository
                .getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_COOKIE)
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(authorizedRedirectUri);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", rawRefreshToken)
                .build().toUriString();

        log.info("OAuth2 login success for user: {}", oAuth2User.getId());
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        cookieRepo.removeAuthorizationRequest(request, response);
        HttpCookieOAuth2AuthorizationRequestRepository
                .deleteCookie(request, response,
                        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_COOKIE);
    }
}
