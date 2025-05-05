package org.treasurehunt.user.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicUserProfile {
    private Long id;
    private String username;
    private String email;
    private Integer score;
}
