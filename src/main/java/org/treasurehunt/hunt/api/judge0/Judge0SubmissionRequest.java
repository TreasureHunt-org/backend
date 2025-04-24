package org.treasurehunt.hunt.api.judge0;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for submitting a code to Judge0 API
 */
public record Judge0SubmissionRequest(
        @JsonProperty("source_code")
        String sourceCode,

        @JsonProperty("language_id")
        String languageId,

        @JsonProperty("stdin")
        String stdin,

        @JsonProperty("expected_output")
        String expectedOutput
) {
}
