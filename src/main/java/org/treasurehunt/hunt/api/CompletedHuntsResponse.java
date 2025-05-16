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
public class CompletedHuntsResponse {
    private List<CompletedHuntItem> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedHuntItem {
        private String id;
        private String title;
        private LocalDate completedDate;
        private int score;
        private int rank;
    }
}