package com.qchat.qchat.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private long accessTokenExpiresIn;   // seconds

    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private UUID id;
        private String username;
        private String email;
        private String displayName;
        private String avatarUrl;
    }
}
