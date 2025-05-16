package org.treasurehunt.user.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserScoreResponse {
    private UserScoreData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserScoreData {
        private int total;
        private int rank;
        private int lastEarned;
        private int highestScore;
    }
}