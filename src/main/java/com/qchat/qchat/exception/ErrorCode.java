package com.stargazer.demo.exception;

import com.stargazer.demo.dto.response.MessageResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    RESOURCE_NOT_FOUND(MessageResponse.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND),
    INVALID_PAGE_NUMBER(MessageResponse.INVALID_PAGE_NUMBER, HttpStatus.BAD_REQUEST),
    LOGIN_FAIL(MessageResponse.LOGIN_FAIL, HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(MessageResponse.USER_NOT_FOUND, HttpStatus.NOT_FOUND),
    EMAIL_EXISTED(MessageResponse.EMAIL_EXISTED, HttpStatus.CONFLICT),
    PHONE_NUMBER_EXISTED(MessageResponse.PHONE_NUMBER_EXISTED, HttpStatus.CONFLICT),
    UNAUTHENTICATED(MessageResponse.UNAUTHENTICATED, HttpStatus.UNAUTHORIZED),
    DENIED_PERMISSION(MessageResponse.DENIED_PERMISSION, HttpStatus.FORBIDDEN),
    WRONG_VARIABLE_TYPE(MessageResponse.WRONG_VARIABLE_TYPE, HttpStatus.BAD_REQUEST),
    INVALID_GRANT(MessageResponse.INVALID_GRANT, HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(MessageResponse.INVALID_TOKEN, HttpStatus.BAD_REQUEST),
    BOOK_ID_NOT_FOUND(MessageResponse.BOOK_IDs_NOT_FOUND, HttpStatus.NOT_FOUND),
    QUANTITY_EXCEED(MessageResponse.QUANTITY_EXCEED, HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_DATA(MessageResponse.INVALID_REQUEST_DATA, HttpStatus.BAD_REQUEST),
    ITEM_NOT_FOUND(MessageResponse.ITEM_NOT_FOUND, HttpStatus.NOT_FOUND),
    PAYMENT_METHOD_NOT_FOUND(MessageResponse.PAYMENT_METHOD_NOT_FOUND, HttpStatus.NOT_FOUND),
    ADDRESS_NOT_FOUND(MessageResponse.ADDRESS_NOT_FOUND, HttpStatus.NOT_FOUND),
    ORDER_ID_NOT_FOUND(MessageResponse.ORDER_ID_NOT_FOUND, HttpStatus.NOT_FOUND),
    CONFLICT_DEFAULT_ADDRESS(MessageResponse.CONFLICT_DEFAULT_ADDRESS, HttpStatus.BAD_REQUEST),
    ORDER_STATUS_ID_NOT_FOUND(MessageResponse.ORDER_STATUS_ID_NOT_FOUND, HttpStatus.NOT_FOUND),
    CATEGORY_EXISTED(MessageResponse.CATEGORY_EXISTED, HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND(MessageResponse.CATEGORY_NOT_FOUND, HttpStatus.NOT_FOUND),
    TARGET_ID_NOT_FOUND(MessageResponse.TARGET_ID_NOT_FOUND, HttpStatus.NOT_FOUND),
    UPLOAD_IMAGE_FAIL(MessageResponse.UPLOAD_IMAGE_FAIL, HttpStatus.EXPECTATION_FAILED),
    INVALID_IMAGE_FILE(MessageResponse.INVALID_IMAGE_FILE, HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_PARAM(MessageResponse.MISSING_REQUIRED_PARAM, HttpStatus.BAD_REQUEST),
    INVALID_MEDIA_TYPE(MessageResponse.INVALID_MEDIA_TYPE, HttpStatus.BAD_REQUEST),
    ORDER_STATUS_INCORRECT_FLOW(MessageResponse.ORDER_STATUS_INCORRECT_FLOW, HttpStatus.BAD_REQUEST),
    ACTIVATE_FAILED(MessageResponse.ACTIVATE_FAILED, HttpStatus.BAD_REQUEST),
    UNACTIVATED_ACCOUNT(MessageResponse.UNACTIVATED_ACCOUNT, HttpStatus.FORBIDDEN),
    ORDER_ITEM_NOT_FOUND(MessageResponse.ORDER_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND),
    ORDER_NOT_COMPLETED_CANNOT_REVIEW(MessageResponse.ORDER_NOT_COMPLETED_CANNOT_REVIEW, HttpStatus.BAD_REQUEST),
    REVIEW_NOT_FOUND(MessageResponse.REVIEW_NOT_FOUND, HttpStatus.NOT_FOUND),
    WRONG_CURRENT_PASSWORD(MessageResponse.WRONG_CURRENT_PASSWORD, HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_MATCHES_OLD(MessageResponse.NEW_PASSWORD_MATCHES_OLD, HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_SET(MessageResponse.PASSWORD_NOT_SET, HttpStatus.BAD_REQUEST),
    PASSWORD_ALREADY_SET(MessageResponse.PASSWORD_ALREADY_SET, HttpStatus.BAD_REQUEST),
    VERIFY_TOKEN_FAILED(MessageResponse.VERIFY_TOKEN_FAILED, HttpStatus.BAD_REQUEST),
    PAGE_NUMBER_OVERFLOW(MessageResponse.PAGE_NUMBER_OVERFLOW, HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_FOUND(MessageResponse.PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND),
    BANNER_NOT_FOUND(MessageResponse.BANNER_NOT_FOUND, HttpStatus.NOT_FOUND),
    USER_ALREADY_HAS_BANK_ACCOUNT(MessageResponse.USER_ALREADY_HAS_BANK_ACCOUNT, HttpStatus.BAD_REQUEST),
    USER_HAS_NO_BANK_ACCOUNT(MessageResponse.USER_HAS_NO_BANK_ACCOUNT, HttpStatus.BAD_REQUEST),
    ORDER_CAN_NOT_CANCEL(MessageResponse.ORDER_CAN_NOT_CANCEL, HttpStatus.BAD_REQUEST),
    REASON_ID_NOT_FOUND(MessageResponse.REASON_ID_NOT_FOUND, HttpStatus.NOT_FOUND),
    CAN_NOT_CREATE_REFUND_LINK(MessageResponse.CAN_NOT_CREATE_REFUND_LINK, HttpStatus.NOT_FOUND)
    ;

    ErrorCode(String message, HttpStatusCode httpStatusCode) {
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    private final String message;
    private final HttpStatusCode httpStatusCode;

}
