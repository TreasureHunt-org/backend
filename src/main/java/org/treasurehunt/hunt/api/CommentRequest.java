package org.treasurehunt.hunt.api;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
    @NotBlank
    String content
) {
}
