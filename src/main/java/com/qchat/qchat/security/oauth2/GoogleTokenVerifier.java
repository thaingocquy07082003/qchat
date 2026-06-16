package com.qchat.qchat.security.oauth2;

import com.qchat.qchat.exception.AppException;
import com.qchat.qchat.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Verifies Google ID tokens issued by the Flutter google_sign_in SDK.
 * Uses Google's tokeninfo endpoint so no extra dependency is needed.
 */
@Component
@Slf4j
public class GoogleTokenVerifier {

    private static final String TOKENINFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token={token}";

    @Value("${google.client-id}")
    private String expectedClientId;

    private final RestClient restClient = RestClient.create();

    /**
     * Verifies the idToken and returns its claims.
     *
     * @throws AppException INVALID_TOKEN if the token is invalid or the audience does not match
     */
    @SuppressWarnings("unchecked")
    public GoogleIdTokenClaims verify(String idToken) {
        Map<String, Object> claims;
        try {
            claims = restClient.get()
                    .uri(TOKENINFO_URL, idToken)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            log.warn("Google tokeninfo call failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if (claims == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        String aud = (String) claims.get("aud");
        if (!expectedClientId.equals(aud)) {
            log.warn("Google token audience mismatch: expected={} actual={}", expectedClientId, aud);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        return GoogleIdTokenClaims.builder()
                .googleId((String) claims.get("sub"))
                .email((String) claims.get("email"))
                .emailVerified("true".equals(String.valueOf(claims.get("email_verified"))))
                .name((String) claims.get("name"))
                .pictureUrl((String) claims.get("picture"))
                .build();
    }

    public record GoogleIdTokenClaims(
            String googleId,
            String email,
            boolean emailVerified,
            String name,
            String pictureUrl
    ) {
        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String googleId, email, name, pictureUrl;
            private boolean emailVerified;

            public Builder googleId(String v)    { this.googleId = v; return this; }
            public Builder email(String v)        { this.email = v; return this; }
            public Builder emailVerified(boolean v) { this.emailVerified = v; return this; }
            public Builder name(String v)         { this.name = v; return this; }
            public Builder pictureUrl(String v)   { this.pictureUrl = v; return this; }

            public GoogleIdTokenClaims build() {
                return new GoogleIdTokenClaims(googleId, email, emailVerified, name, pictureUrl);
            }
        }
    }
}
