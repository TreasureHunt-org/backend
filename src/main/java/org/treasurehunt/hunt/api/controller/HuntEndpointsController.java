package org.treasurehunt.hunt.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.treasurehunt.hunt.api.ActiveHuntsResponse;
import org.treasurehunt.hunt.api.CompletedHuntsResponse;
import org.treasurehunt.hunt.service.HuntService;

@RestController
@RequestMapping("/hunts")
@RequiredArgsConstructor
public class HuntEndpointsController {

    private final HuntService huntService;

    @GetMapping("/active")
    public ResponseEntity<ActiveHuntsResponse> getActiveHunts() {
        return ResponseEntity.ok(huntService.getActiveHunts());
    }

    @GetMapping("/completed")
    public ResponseEntity<CompletedHuntsResponse> getCompletedHunts() {
        return ResponseEntity.ok(huntService.getCompletedHunts());
    }
}
