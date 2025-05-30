package org.treasurehunt.requestedcodes.repo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "requested_codes", schema = "treasure-hunt")
public class RequestedCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @JoinColumn(name = "hunt_id", nullable = false)
    private Long hunt_id;

    @Size(max = 45)
    @NotNull
    @Column(name = "code", nullable = false, length = 45)
    private String code;

    @Column(name = "consumed")
    private boolean consumed;

}