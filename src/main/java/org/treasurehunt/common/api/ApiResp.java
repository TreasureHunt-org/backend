package org.treasurehunt.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        oneOf = {
                ApiResp.SuccessExample.class,
                ApiResp.ErrorExample.class
        }
)
@Getter
@Builder
public class ApiResp<T> {
    private final boolean success;
    private final String message;
    private final List<T> data;
    @Singular
    private final List<String> errors;
    private final int errorCode;
    private final long timestamp;

    public static <T> ApiResp<T> success(List<T> data, String message) {
        return ApiResp.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResp<T> error(String message, List<String> errors, int errorCode) {
        return ApiResp.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .errors(errors)
                .errorCode(errorCode)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResp<T> error(String message, String error, int errorCode) {
        return ApiResp.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .error(error)
                .errorCode(errorCode)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // Inner classes for examples
    @Schema(name = "SuccessResponse")
    public static class SuccessExample {
        @Schema(example = "true")
        public boolean success;

        @Schema(example = "Operation completed successfully")
        public String message;

        @Schema(example = "[{\"id\": 1, \"name\": \"Example\"}]")
        public List<?> data;

        @Schema(example = "null")
        public List<String> errors;

        @Schema(example = "0")
        public int errorCode;

        @Schema(example = "1625097600000")
        public long timestamp;
    }

    @Schema(name = "ErrorResponse")
    public static class ErrorExample {
        @Schema(example = "false")
        public boolean success;

        @Schema(example = "An error occurred")
        public String message;

        @Schema(example = "null")
        public List<?> data;

        @Schema(example = "[\"Validation failed\", \"Field 'name' is required\"]")
        public List<String> errors;

        @Schema(example = "400")
        public int errorCode;

        @Schema(example = "1625097600000")
        public long timestamp;
    }
}