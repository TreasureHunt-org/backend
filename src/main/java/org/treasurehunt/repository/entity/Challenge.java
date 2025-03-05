package org.treasurehunt.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "challenge")
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_type_id", nullable = false)
    private ChallengeType challengeType;

    @OneToMany(
            mappedBy = "challenge",
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<ChallengeCode> challengeCodes;

    @Size(max = 2083)
    @Column(name = "map_piece_uri", length = 2083)
    private String mapPieceUri;

    @Size(max = 2083)
    @Column(name = "external_game_uri", length = 2083)
    private String externalGameUri;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "test_cases")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> testCases;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}