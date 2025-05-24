package org.treasurehunt.submissions.api;

import lombok.Builder;
import lombok.Data;
import org.treasurehunt.submissions.repo.Submission;

import java.time.Instant;

@Data
@Builder
public class SubmissionResponseDTO {
    private String id;
    private String hunterName;
    private Instant submissionDate;
    private Integer challengeNumber;
    private String challengeTitle;
    private Integer score;
    private String status;
    private String code;
    private String huntId;
    private String huntName;
}