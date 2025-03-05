package org.treasurehunt.common.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;


/**
 * A generic API response wrapper that standardizes the structure of API responses.
 *
 * <p>This class provides a consistent way to return data, success messages, and error details
 * in a structured format, ensuring clarity in API responses.</p>
 *
 * <h5>Example Usage:</h3>
 *
 * <p>✅ Success Response:</p>
 * <pre>{@code
 * ApiResponse<UserDto> successResponse = ApiResponse.<UserDto>builder()
 *     .success(true)
 *     .message("User retrieved successfully")
 *     .data(userDto)
 *     .timestamp(System.currentTimeMillis())
 *     .build();
 * }</pre>
 *
 * <p>❌ Error Response (Multiple Errors):</p>
 * <pre>{@code
 * ApiResponse<Object> errorResponse = ApiResponse.builder()
 *     .success(false)
 *     .message("Validation failed")
 *     .error("Email is required")
 *     .error("Password is too short")
 *     .errorCode(400)
 *     .timestamp(System.currentTimeMillis())
 *     .build();
 * }</pre>
 *
 * <p>❌ Error Response (Single Error):</p>
 * <pre>{@code
 * ApiResponse<Object> singleErrorResponse = ApiResponse.builder()
 *     .success(false)
 *     .message("Unauthorized")
 *     .error("Invalid credentials")
 *     .errorCode(401)
 *     .build();
 * }</pre>
 *
 * @param <T> The type of data returned in the response.
 * @author Rashed Al Maaitah
 * @version 1.0
 */
@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final List<T> data;
    @Singular
    private final List<String> errors;
    private final int errorCode;
    private final long timestamp;

    public static <T> ApiResponse<T> success(List<T> data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, List<String> errors, int errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .errorCode(errorCode)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}