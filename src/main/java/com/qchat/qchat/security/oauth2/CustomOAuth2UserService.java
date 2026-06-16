package com.qchat.qchat.security.oauth2;

import com.qchat.qchat.auth.entity.User;
import com.qchat.qchat.auth.entity.UserProfile;
import com.qchat.qchat.auth.enums.OAuthProvider;
import com.qchat.qchat.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, oAuth2User.getAttributes());

        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());
        User user = userRepository.findByOauthProviderAndOauthId(provider, userInfo.getId())
                .map(existing -> updateOAuth2User(existing, userInfo))
                .orElseGet(() -> registerOAuth2User(provider, userInfo));

        return new CustomOAuth2User(user, oAuth2User.getAttributes(), nameAttributeKey);
    }

    private User registerOAuth2User(OAuthProvider provider, OAuth2UserInfo userInfo) {
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);

        UserProfile profile = UserProfile.builder()
                .displayName(userInfo.getName())
                .avatarUrl(userInfo.getAvatarUrl())
                .build();

        User user = User.builder()
                .username(username)
                .email(userInfo.getEmail())
                .oauthProvider(provider)
                .oauthId(userInfo.getId())
                .isEmailVerified(true)
                .profile(profile)
                .build();

        profile.setUser(user);
        log.info("Registering new OAuth2 user: {} via {}", username, provider);
        return userRepository.save(user);
    }

    private User updateOAuth2User(User existing, OAuth2UserInfo userInfo) {
        UserProfile profile = existing.getProfile();
        if (profile != null) {
            profile.setAvatarUrl(userInfo.getAvatarUrl());
        }
        return userRepository.save(existing);
    }
}
