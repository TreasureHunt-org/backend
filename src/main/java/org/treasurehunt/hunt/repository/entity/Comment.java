package org.treasurehunt.hunt.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.treasurehunt.user.repository.entity.User;

@Entity
@Getter
@Setter
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "hunt_id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Hunt hunt;

    @JoinColumn(name = "reviewer_id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User reviewer;

    @Lob
    @Column(name = "content", length = Integer.MAX_VALUE, nullable = false)
    private String content;

}
