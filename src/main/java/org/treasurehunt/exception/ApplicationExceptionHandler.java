package org.treasurehunt.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.treasurehunt.common.api.ApiResp;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Log4j2
@RestControllerAdvice
@RequiredArgsConstructor
public class ApplicationExceptionHandler {

    private final HttpServletRequest request;

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<ApiResp<String>> handleEntityAlreadyExistsException(
            EntityAlreadyExistsException ex) {
        log.error("Entity already exists exception {} {} \n", request.getRequestURI(), ex);

        return ResponseEntity.status(CONFLICT)
                .body(
                        ApiResp.error("Entity already exists",
                                List.of(ex.getMessage()),
                                CONFLICT.value())
                );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResp<String>> handleResourceNotFoundException(
            RuntimeException ex) {
        log.error("Resource not found {} \n", request.getRequestURI(), ex);

        return ResponseEntity.status(NOT_FOUND)
                .body(
                        ApiResp.error("Entity not found",
                                List.of(ex.getMessage()),
                                NOT_FOUND.value()));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiResp<String>> handleAuthenticationException(
            AuthenticationFailedException ex) {
        log.error("Authentication failed: {} \n", request.getRequestURI(), ex);

        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ApiResp.error("Authentication failed",
                                List.of(ex.getMessage()),
                                UNAUTHORIZED.value()));
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiResp<String>> handleRefreshTokenException(
            RefreshTokenException ex) {
        log.error("Refresh Token exception: {} \n", request.getRequestURI(), ex);

        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ApiResp.error("Invalid Refresh Token",
                                List.of(ex.getMessage()),
                                UNAUTHORIZED.value()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResp<String>> handleAccessDeniedException(
            AccessDeniedException ex) {
        log.error("Access denied error: {} \n", request.getRequestURI(), ex);

        return ResponseEntity
                .status(FORBIDDEN)
                .body(
                        ApiResp.error("Access denied.",
                                List.of(ex.getMessage()),
                                FORBIDDEN.value()));
    }

//    @ExceptionHandler(IncorrectPasswordException.class)
//    public ResponseEntity<ApiResponse<String>> handleIncorrectPasswordException(
//            IncorrectPasswordException ex
//    ) {
//        log.error("Incorrect password error: {} \n", request.getRequestURI(), ex);
//
//        return ResponseEntity
//                .status(FORBIDDEN)
//                .body(
//                        ApiResponse.error("Incorrect password",
//                                List.of(ex.getMessage()),
//                                FORBIDDEN.value(), request.getRequestURI()));
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResp<String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        log.error("Method Argument Not Valid Exception: {} \n", request.getRequestURI(), ex);

        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ApiResp.error("Validation errors occurred",
                                errors,
                                BAD_REQUEST.value()
                        )
                );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResp<String>> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("ConstraintViolationException: {}", ex.getMessage(), ex);
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResp.error("Validation failed", errors, BAD_REQUEST.value()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResp<String>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResp.error("Bad Request", ex.getMessage(), BAD_REQUEST.value()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResp<String>> handleMissingServletRequestPartException(
            MissingServletRequestPartException ex) {
        log.error("Missing required request part: {} \n", request.getRequestURI(), ex);

        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResp.error("Missing required request part",
                        List.of(ex.getMessage()),
                        BAD_REQUEST.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResp<String>> handleGeneralException(
            Exception ex) {
        log.error("Unexpected error: {} \n", request.getRequestURI(), ex);

        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ApiResp.error("An unexpected error occurred",
                                List.of(ex.getMessage()),
                                INTERNAL_SERVER_ERROR.value()));
    }

}
