package org.treasurehunt.hunt.api.judge0;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for Judge0 API submission
 */
public record Judge0SubmissionResponse(
        @JsonProperty("token")
        String token,
        
        @JsonProperty("status")
        Status status,
        
        @JsonProperty("stdout")
        String stdout,
        
        @JsonProperty("stderr")
        String stderr,
        
        @JsonProperty("compile_output")
        String compileOutput,
        
        @JsonProperty("message")
        String message,
        
        @JsonProperty("time")
        String time,
        
        @JsonProperty("memory")
        Integer memory
) {
    /**
     * Status of the submission
     */
    public record Status(
            @JsonProperty("id")
            Integer id,
            
            @JsonProperty("description")
            String description
    ) {
    }
}