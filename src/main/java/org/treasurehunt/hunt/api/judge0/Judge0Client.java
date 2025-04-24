package org.treasurehunt.hunt.api.judge0;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * OpenFeign client for Judge0 API
 */
@FeignClient(name = "judge0", url = "${judge0.api.url}")
public interface Judge0Client {

    /**
     * Submits code to Judge0 for execution
     *
     * @param apiKey  the RapidAPI key
     * @param apiHost the RapidAPI host
     * @param request the submission request
     * @return the submission response
     */
    @PostMapping("/submissions?base64_encoded=true&wait=true")
    Judge0SubmissionResponse submitCode(
            @RequestHeader("X-RapidAPI-Key") String apiKey,
            @RequestHeader("X-RapidAPI-Host") String apiHost,
            @RequestBody Judge0SubmissionRequest request
    );
}