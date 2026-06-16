package com.qchat.qchat.exception;

import com.qchat.qchat.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        log.warn("AppException: {}", ex.getMessage());
        ErrorCode code = ex.getErrorCode();
        return ResponseEntity
                .status(code.getHttpStatusCode())
                .body(ApiResponse.error(((org.springframework.http.HttpStatus) code.getHttpStatusCode()).value(), code.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        ErrorCode code = ErrorCode.DENIED_PERMISSION;
        return ResponseEntity
                .status(code.getHttpStatusCode())
                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), code.getMessage()));
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), ErrorCode.UNAUTHENTICATED.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorCode code = ErrorCode.WRONG_VARIABLE_TYPE;
        return ResponseEntity
                .status(code.getHttpStatusCode())
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), code.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        ErrorCode code = ErrorCode.INVALID_REQUEST_DATA;
        return ResponseEntity
                .status(code.getHttpStatusCode())
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), code.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "";
        if (msg.contains("users_email_key") || msg.contains("email")) {
            ErrorCode code = ErrorCode.EMAIL_EXISTED;
            return ResponseEntity.status(code.getHttpStatusCode())
                    .body(ApiResponse.error(HttpStatus.CONFLICT.value(), code.getMessage()));
        }
        if (msg.contains("users_phone_number_key") || msg.contains("phone")) {
            ErrorCode code = ErrorCode.PHONE_NUMBER_EXISTED;
            return ResponseEntity.status(code.getHttpStatusCode())
                    .body(ApiResponse.error(HttpStatus.CONFLICT.value(), code.getMessage()));
        }
        if (msg.contains("users_username_key") || msg.contains("username")) {
            ErrorCode code = ErrorCode.USERNAME_EXISTED;
            return ResponseEntity.status(code.getHttpStatusCode())
                    .body(ApiResponse.error(HttpStatus.CONFLICT.value(), code.getMessage()));
        }
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), ErrorCode.INVALID_REQUEST_DATA.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), ErrorCode.RESOURCE_NOT_FOUND.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobal(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.SERVER_ERROR.getMessage()));
    }
}
