package org.treasurehunt.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "challenge_code")
public class ChallengeCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_code_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Size(max = 50)
    @NotNull
    @Column(name = "language", nullable = false, length = 50)
    private String language;

    @NotNull
    @Lob
    @Column(name = "code", nullable = false)
    private String code;

}