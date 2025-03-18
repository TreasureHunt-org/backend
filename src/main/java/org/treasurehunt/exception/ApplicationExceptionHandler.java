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
import org.treasurehunt.common.api.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Log4j2
@RestControllerAdvice
@RequiredArgsConstructor
public class ApplicationExceptionHandler {

    private final HttpServletRequest request;

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleEntityAlreadyExistsException(
            EntityAlreadyExistsException ex) {
        log.error("Entity already exists exception {} {} \n", request.getRequestURI(), ex);

        return ResponseEntity.status(CONFLICT)
                .body(
                        ApiResponse.error("Entity already exists",
                                List.of(ex.getMessage()),
                                CONFLICT.value())
                );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceNotFoundException(
            RuntimeException ex) {
        log.error("Resource not found {} \n", request.getRequestURI(), ex);

        return ResponseEntity.status(NOT_FOUND)
                .body(
                        ApiResponse.error("Entity not found",
                                List.of(ex.getMessage()),
                                NOT_FOUND.value()));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(
            AuthenticationFailedException ex) {
        log.error("Authentication failed: {} \n", request.getRequestURI(), ex);

        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ApiResponse.error("Authentication failed",
                                List.of(ex.getMessage()),
                                UNAUTHORIZED.value()));
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiResponse<String>> handleRefreshTokenException(
            RefreshTokenException ex) {
        log.error("Refresh Token exception: {} \n", request.getRequestURI(), ex);

        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ApiResponse.error("Invalid Refresh Token",
                                List.of(ex.getMessage()),
                                UNAUTHORIZED.value()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(
            AccessDeniedException ex) {
        log.error("Access denied error: {} \n", request.getRequestURI(), ex);

        return ResponseEntity
                .status(FORBIDDEN)
                .body(
                        ApiResponse.error("Access denied.",
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
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        log.error("Method Argument Not Valid Exception: {} \n", request.getRequestURI(), ex);

        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ApiResponse.error("Validation errors occurred",
                                errors,
                                BAD_REQUEST.value()
                        )
                );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("ConstraintViolationException: {}", ex.getMessage(), ex);
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors, BAD_REQUEST.value()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(
            Exception ex) {
        log.error("Unexpected error: {} \n", request.getRequestURI(), ex);

        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse.error("An unexpected error occurred",
                                List.of(ex.getMessage()),
                                INTERNAL_SERVER_ERROR.value()));
    }

}