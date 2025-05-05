package org.treasurehunt.hunt.api;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeInfo{
        private Long pointsCollected;
        private List<ChallengeState> challenges;
}
