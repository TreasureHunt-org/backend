package org.treasurehunt.hunt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.treasurehunt.common.enums.SupportedLanguages;
import org.treasurehunt.exception.BadRequestException;
import org.treasurehunt.hunt.api.SubmitSolutionResponse.TestCaseResult;
import org.treasurehunt.hunt.api.judge0.Judge0Client;
import org.treasurehunt.hunt.api.judge0.Judge0SubmissionRequest;
import org.treasurehunt.hunt.api.judge0.Judge0SubmissionResponse;
import org.treasurehunt.hunt.repository.entity.TestCase;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class Judge0Service {

    private final Judge0Client judge0Client;

    @Value("${judge0.api.key}")
    private String judge0ApiKey;

    @Value("${judge0.api.host}")
    private String judge0ApiHost;

    /**
     * Submits code to Judge0 for validation against test cases
     *
     * @param sourceCode the source code to validate
     * @param language   the programming language
     * @param testCases  the test cases to validate against
     * @return a list of test case results
     */
    public List<TestCaseResult> validateCode(String sourceCode, SupportedLanguages language, List<TestCase> testCases) {
        log.info("Validating code in {} against {} test cases", language, testCases.size());

        // Validate input parameters
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            throw new BadRequestException("Source code cannot be empty");
        }

        if (language == null) {
            throw new BadRequestException("Programming language must be specified");
        }

        List<TestCaseResult> results = new ArrayList<>();

        for (TestCase testCase : testCases) {
            try {
                // Base64 encode the source code, input, and expected output
                String encodedSourceCode = Base64.getEncoder().encodeToString(sourceCode.getBytes());
                String encodedInput = Base64.getEncoder().encodeToString(testCase.getInput().getBytes());
                String encodedExpectedOutput = Base64.getEncoder().encodeToString(testCase.getExpectedOutput().getBytes());

                Judge0SubmissionRequest request = new Judge0SubmissionRequest(
                        encodedSourceCode,
                        String.valueOf(language.getLanguageId()),
                        encodedInput,
                        encodedExpectedOutput
                );

                // Use the OpenFeign client to make the API call
                Judge0SubmissionResponse response = judge0Client.submitCode(
                        judge0ApiKey,
                        judge0ApiHost,
                        request
                );

                if (response == null) {
                    throw new BadRequestException("Failed to get response from Judge0 API");
                }

                // Decode the stdout if it's not null
                String decodedStdout = null;
                if (response.stdout() != null && !response.stdout().isEmpty()) {
                    decodedStdout = new String(Base64.getDecoder().decode(response.stdout()));
                }

                boolean passed = isSubmissionSuccessful(response) && 
                        (decodedStdout != null && decodedStdout.trim().equals(testCase.getExpectedOutput().trim()));

                String error = getErrorMessage(response);

                results.add(new TestCaseResult(
                        testCase.getInput(),
                        testCase.getExpectedOutput(),
                        decodedStdout,
                        passed,
                        error
                ));

            } catch (Exception e) {
                log.error("Error validating code against test case: {}", e.getMessage(), e);
                results.add(new TestCaseResult(
                        testCase.getInput(),
                        testCase.getExpectedOutput(),
                        null,
                        false,
                        "Error processing submission: " + e.getMessage()
                ));
            }
        }

        return results;
    }

    private boolean isSubmissionSuccessful(Judge0SubmissionResponse response) {
        // Status ID 3 means "Accepted" in Judge0
        return response.status() != null && response.status().id() == 3;
    }

    private String getErrorMessage(Judge0SubmissionResponse response) {
        if (response.status() == null) {
            return "Unknown error";
        }

        if (response.status().id() != 3) {
            StringBuilder error = new StringBuilder(response.status().description());

            if (response.compileOutput() != null && !response.compileOutput().isEmpty()) {
                try {
                    String decodedCompileOutput = new String(Base64.getDecoder().decode(response.compileOutput()));
                    error.append(": ").append(decodedCompileOutput);
                } catch (IllegalArgumentException e) {
                    // If decoding fails, use the raw value
                    error.append(": ").append(response.compileOutput());
                    log.warn("Failed to decode compile output: {}", e.getMessage());
                }
            }

            if (response.stderr() != null && !response.stderr().isEmpty()) {
                try {
                    String decodedStderr = new String(Base64.getDecoder().decode(response.stderr()));
                    error.append(": ").append(decodedStderr);
                } catch (IllegalArgumentException e) {
                    // If decoding fails, use the raw value
                    error.append(": ").append(response.stderr());
                    log.warn("Failed to decode stderr: {}", e.getMessage());
                }
            }

            return error.toString();
        }

        return null;
    }
}
