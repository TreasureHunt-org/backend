package org.treasurehunt.submissions.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SubmissionListResponse {
    private boolean success;
    private long totalPages;
    private List<SubmissionResponseDTO> data;
}