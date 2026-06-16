package com.qchat.qchat.auth.service;

import com.qchat.qchat.auth.dto.request.LoginRequest;
import com.qchat.qchat.auth.dto.request.RegisterRequest;
import com.qchat.qchat.auth.dto.request.TokenRefreshRequest;
import com.qchat.qchat.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String deviceInfo);

    AuthResponse refresh(TokenRefreshRequest request);

    void logout(String rawRefreshToken, String rawAccessToken);
}
