package org.treasurehunt.hunt.api;

import java.util.List;

/**
 * Response DTO for submitting a solution to a challenge
 */
public record SubmitSolutionResponse(
        Long challengeId,
        boolean success,
        String message,
        List<TestCaseResult> testCaseResults
) {
    /**
     * Result of a test case execution
     */
    public record TestCaseResult(
            String input,
            String expectedOutput,
            String actualOutput,
            boolean passed,
            String error
    ) {
    }
}