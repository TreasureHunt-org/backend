package org.treasurehunt.hunt.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveHuntsResponse {
    private List<ActiveHuntItem> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveHuntItem {
        private String id;
        private String title;
        private double progress;
        private int completedChallenges;
        private int totalChallenges;
        private LocalDate dueDate;
    }
}