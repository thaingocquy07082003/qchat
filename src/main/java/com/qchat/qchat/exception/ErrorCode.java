package com.qchat.qchat.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    // Generic
    SERVER_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_NOT_FOUND("Resource not found", HttpStatus.NOT_FOUND),
    INVALID_REQUEST_DATA("Invalid request data", HttpStatus.BAD_REQUEST),
    WRONG_VARIABLE_TYPE("Wrong variable type", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_PARAM("Missing required parameter", HttpStatus.BAD_REQUEST),
    INVALID_MEDIA_TYPE("Unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    // Auth
    LOGIN_FAIL("Invalid credentials", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED("Authentication required", HttpStatus.UNAUTHORIZED),
    DENIED_PERMISSION("Access denied", HttpStatus.FORBIDDEN),
    INVALID_TOKEN("Invalid or expired token", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED("Token has been revoked", HttpStatus.UNAUTHORIZED),
    INVALID_GRANT("Invalid grant", HttpStatus.BAD_REQUEST),

    // User
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
    USERNAME_EXISTED("Username already exists", HttpStatus.CONFLICT),
    EMAIL_EXISTED("Email already exists", HttpStatus.CONFLICT),
    PHONE_NUMBER_EXISTED("Phone number already exists", HttpStatus.CONFLICT),
    INVALID_PAGE_NUMBER("Invalid page number", HttpStatus.BAD_REQUEST),

    // Chat
    CONVERSATION_NOT_FOUND("Conversation not found", HttpStatus.NOT_FOUND),
    NOT_CONVERSATION_MEMBER("You are not a member of this conversation", HttpStatus.FORBIDDEN),
    CANNOT_MESSAGE_SELF("Cannot start a conversation with yourself", HttpStatus.BAD_REQUEST),
    ;

    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(String message, HttpStatusCode httpStatusCode) {
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
