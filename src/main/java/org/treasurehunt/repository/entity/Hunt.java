package org.treasurehunt.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.treasurehunt.common.enums.HuntStatus;
import org.treasurehunt.user.User;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "hunt")
public class Hunt {
    @Id
    @Column(name = "hunt_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @OneToMany(
            mappedBy = "hunt",
            fetch = FetchType.LAZY
    )
    private List<User> participants;

    @ManyToMany(
            fetch = FetchType.LAZY,
            mappedBy = "huntsToReview"
    )
    private List<Reviewer> reviewers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HuntStatus status;

    @Size(max = 2083)
    @Column(name = "hunt_img_uri", length = 2083)
    private String huntImgUri;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToMany(
            mappedBy = "hunt",
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    private List<Challenge> challenges;

}