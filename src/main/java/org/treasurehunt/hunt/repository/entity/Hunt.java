package org.treasurehunt.hunt.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.treasurehunt.common.enums.HuntStatus;
import org.treasurehunt.user.repository.entity.User;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hunt")
public class Hunt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @OneToMany(mappedBy = "hunt", fetch = FetchType.LAZY)
    private List<User> participants;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(name = "start_date")
    @Future
    private Instant startDate;

    @Column(name = "end_date")
    @Future
    private Instant endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HuntStatus status;

    @Size(max = 2083)
    @Column(name = "hunt_img_uri", length = 2083)
    private String huntImgUri;

    @Size(max = 2083)
    @Column(name = "map_img_uri", length = 2083)
    private String mapImgUri;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToMany(
            mappedBy = "hunt",
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<Challenge> challenges;

}