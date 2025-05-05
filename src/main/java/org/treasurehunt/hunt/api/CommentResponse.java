package org.treasurehunt.hunt.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponse {
    Long id;
    Long huntId;
    Long reviewerId;
    String content;
}
