package com.qchat.qchat.security.oauth2;

import com.qchat.qchat.security.CustomUserDetails;
import lombok.Getter;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;

/**
 * Wraps CustomUserDetails so it can be used as both UserDetails and OAuth2User
 * inside the OAuth2 success handler.
 */
@Getter
public class CustomOAuth2User extends CustomUserDetails implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    public CustomOAuth2User(com.qchat.qchat.auth.entity.User user,
                            Map<String, Object> attributes,
                            String nameAttributeKey) {
        super(user);
        this.attributes       = attributes;
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return super.getAuthorities();
    }

    @Override
    public String getName() {
        return String.valueOf(attributes.get(nameAttributeKey));
    }
}
