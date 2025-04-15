package org.treasurehunt.hunt.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestCaseDTO {

    @NotNull(message = "Input is required")
    private String input;

    @NotNull(message = "Expected output is required")
    private String expectedOutput;

    @NotNull(message = "Order is required")
    private Integer order;
}