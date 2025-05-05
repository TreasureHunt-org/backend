package org.treasurehunt.submissions.repo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "challenge_id")
    private Long challengeId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "code")
    private String code;

    @Column(name = "language")
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubmissionStatus status;

    @Column(name = "time")
    private Instant time;

    public enum SubmissionStatus{
        SUCCESS,
        FAIL
    }
}


