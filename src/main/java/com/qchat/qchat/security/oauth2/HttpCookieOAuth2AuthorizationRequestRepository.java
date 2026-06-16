package com.qchat.qchat.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * Stores the OAuth2 authorization request in a short-lived cookie so the API
 * layer stays fully stateless (no HTTP session required).
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final  String OAUTH2_REQUEST_COOKIE = "oauth2_auth_request";
    public static final  String REDIRECT_URI_COOKIE   = "redirect_after_login";
    private static final int    COOKIE_EXPIRE_SECONDS  = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request, OAUTH2_REQUEST_COOKIE)
                .map(c -> (OAuth2AuthorizationRequest) deserialize(c.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authRequest == null) {
            deleteCookie(request, response, OAUTH2_REQUEST_COOKIE);
            deleteCookie(request, response, REDIRECT_URI_COOKIE);
            return;
        }

        addCookie(response, OAUTH2_REQUEST_COOKIE,
                serialize(authRequest), COOKIE_EXPIRE_SECONDS);

        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri != null && !redirectUri.isBlank()) {
            addCookie(response, REDIRECT_URI_COOKIE, redirectUri, COOKIE_EXPIRE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
        deleteCookie(request, response, OAUTH2_REQUEST_COOKIE);
        return req;
    }

    // ── Cookie helpers ──────────────────────────────────────────

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies).filter(c -> c.getName().equals(name)).findFirst();
    }

    private static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return;
        Arrays.stream(cookies)
                .filter(c -> c.getName().equals(name))
                .forEach(c -> {
                    c.setValue("");
                    c.setPath("/");
                    c.setMaxAge(0);
                    response.addCookie(c);
                });
    }

    // ── Java serialization (OAuth2AuthorizationRequest implements Serializable) ──

    private static String serialize(Object object) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize OAuth2AuthorizationRequest", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserialize(String value) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getUrlDecoder().decode(value));
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Failed to deserialize OAuth2AuthorizationRequest", e);
        }
    }
}
