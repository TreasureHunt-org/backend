package org.treasurehunt.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.treasurehunt.user.User;

import java.util.List;

@Entity
@Getter
@Setter
//@Table(name = "reviewer")
public class Reviewer extends User {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hunt_reviewers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "hunt_id")
    )
    private List<Hunt> huntsToReview;
}
