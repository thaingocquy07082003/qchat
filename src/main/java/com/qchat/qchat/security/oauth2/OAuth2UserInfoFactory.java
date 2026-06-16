package com.qchat.qchat.security.oauth2;

import com.qchat.qchat.auth.enums.OAuthProvider;
import com.qchat.qchat.exception.AppException;
import com.qchat.qchat.exception.ErrorCode;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());
        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            default -> throw new AppException(ErrorCode.INVALID_GRANT);
        };
    }

    private OAuth2UserInfoFactory() {}
}
