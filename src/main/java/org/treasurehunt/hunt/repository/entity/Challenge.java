package org.treasurehunt.hunt.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.treasurehunt.common.enums.ChallengeType;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Table(name = "challenge" )
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hunt_id", nullable = false)
    private Hunt hunt;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "points", nullable = false)
    private Integer points;

    @NotNull
    @Column(name = "challenge_type", nullable = false)
    ChallengeType challengeType;

    @OneToMany(
            mappedBy = "challenge",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ChallengeCode> challengeCodes;

    @OneToMany(
            mappedBy = "challenge",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OptimalSolution> optimalSolutions;

    @Size(max = 2083)
    @Column(name = "map_piece_uri", length = 2083)
    private String mapPieceUri;

    @Size(max = 2083)
    @Column(name = "external_game_uri", length = 2083)
    private String externalGameUri;

    @Lob
    @NotNull
    @Column(name = "description" )
    private String description;

    @OneToMany(
            mappedBy = "challenge",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<TestCase> testCases;

    @ColumnDefault("CURRENT_TIMESTAMP" )
    @Column(name = "created_at" )
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP" )
    @Column(name = "updated_at" )
    private Instant updatedAt;

}