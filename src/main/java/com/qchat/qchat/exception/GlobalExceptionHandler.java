package com.stargazer.demo.exception;

import com.stargazer.demo.dto.response.APIResponse;
import com.stargazer.demo.dto.response.MessageResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<APIResponse> handleGlobalException(Exception exception) {
        log.error("Exception: {}", String.valueOf(exception));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new APIResponse(MessageResponse.SERVER_ERROR, exception.getMessage()));
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<APIResponse> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(ErrorCode.DENIED_PERMISSION.getHttpStatusCode())
                .body(new APIResponse(ErrorCode.DENIED_PERMISSION.getMessage(), exception.getMessage()));
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<APIResponse> handleAppException(AppException exception) {
        log.error("Exception: {}", exception.getMessage());
        return ResponseEntity.status(exception.getErrorCode().getHttpStatusCode())
                .body(new APIResponse(exception.getErrorCode().getMessage(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<APIResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        return ResponseEntity.status(ErrorCode.WRONG_VARIABLE_TYPE.getHttpStatusCode())
                .body(new APIResponse(ErrorCode.WRONG_VARIABLE_TYPE.getMessage(), ex.getMessage()));
    }

    @ExceptionHandler(FeignException.BadRequest.class)
    public ResponseEntity<APIResponse> handleInvalidGrantException(FeignException.BadRequest e) {

        return ResponseEntity.status(ErrorCode.INVALID_GRANT.getHttpStatusCode())
                .body(new APIResponse(ErrorCode.INVALID_GRANT.getMessage(), e.getMessage()));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleInvalidArgument(MethodArgumentNotValidException exception) {
        String message = exception.getMessage();
        log.error("Exception: {}", message);
        if (message.contains("Phone number must only contain digits")) {
            return ResponseEntity.status(ErrorCode.INVALID_REQUEST_DATA.getHttpStatusCode())
                    .body(new APIResponse(ErrorCode.INVALID_REQUEST_DATA.getMessage(),
                            "Phone number must only contain digits"));
        }
        else if (message.contains("Phone number must be exactly 10 digits")) {
            return ResponseEntity.status(ErrorCode.INVALID_REQUEST_DATA.getHttpStatusCode())
                    .body(new APIResponse(ErrorCode.INVALID_REQUEST_DATA.getMessage(),
                            "Phone number must be exactly 10 digits"));
        }
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST_DATA.getHttpStatusCode())
                .body(new APIResponse(ErrorCode.INVALID_REQUEST_DATA.getMessage(), message));
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<APIResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST_DATA.getHttpStatusCode())
                .body(new APIResponse(ErrorCode.INVALID_REQUEST_DATA.getMessage(), exception.getMessage()));
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        if (message.contains("UniqueEmail")) {
            return ResponseEntity.status(ErrorCode.EMAIL_EXISTED.getHttpStatusCode())
                    .body(new APIResponse(ErrorCode.EMAIL_EXISTED.getMessage(), null));
        }
        else if (message.contains("UniquePhoneNumber")) {
            return ResponseEntity.status(ErrorCode.PHONE_NUMBER_EXISTED.getHttpStatusCode())
                    .body(new APIResponse(ErrorCode.PHONE_NUMBER_EXISTED.getMessage(), null));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse(MessageResponse.INVALID_REQUEST_DATA, ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<APIResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatusCode())
                .body(new APIResponse(MessageResponse.RESOURCE_NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<APIResponse> handleMissingRequestPart(MissingServletRequestPartException ex) {
        return ResponseEntity.status(ErrorCode.MISSING_REQUIRED_PARAM.getHttpStatusCode())
                .body(new APIResponse(MessageResponse.MISSING_REQUIRED_PARAM, ex.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<APIResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(ErrorCode.INVALID_MEDIA_TYPE.getHttpStatusCode())
                .body(new APIResponse(MessageResponse.INVALID_MEDIA_TYPE, ex.getMessage()));
    }
}
