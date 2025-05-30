package org.treasurehunt.requestedcodes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.treasurehunt.requestedcodes.service.RequestedCodesService;

import java.util.Map;

@Controller
@RequestMapping("requested-codes")
@Tag(name = "Requested Codes", description = "API for requesting hunt codes")
public class RequestedCodesController {

    private final RequestedCodesService requestedCodesService;

    public RequestedCodesController(RequestedCodesService requestedCodesService) {
        this.requestedCodesService = requestedCodesService;
    }

    @Operation(
            summary = "Request a code for a hunt",
            description = "Generates or retrieves a unique code for the specified hunt ID"
    )
    @PostMapping("{huntId}")
    public ResponseEntity<String> requestCode(
            @Parameter(description = "The ID of the hunt for which a code is requested", required = true)
            @PathVariable(name = "huntId") long huntId
    ){
        return ResponseEntity.ok(requestedCodesService.requestCode(huntId));
    }

    @Operation(
            summary = "Get the code for a hunt",
            description = "Fetches the previously generated code for the specified hunt ID"
    )
    @GetMapping("{huntId}")
    public ResponseEntity<String> getCode(
            @Parameter(description = "The ID of the hunt whose code is being retrieved", required = true)
            @PathVariable(name = "huntId") long huntId
    ) {
        return ResponseEntity.ok(requestedCodesService.getCode(huntId));
    }

    @Operation(
            summary = "Submit the found code."
    )
    @PostMapping("{huntId}/submit")
    public ResponseEntity<Map<String, Object>> submitRequestedCode(
            @PathVariable long huntId,
            @NotBlank @RequestParam String code
    ){
        return ResponseEntity.ok(requestedCodesService.submitCode(huntId, code));
    }
}
