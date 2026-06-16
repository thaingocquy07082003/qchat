package com.qchat.qchat.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    /** email, username, hoặc số điện thoại */
    @NotBlank(message = "Identity is required")
    private String identity;

    @NotBlank(message = "Password is required")
    private String password;
}
