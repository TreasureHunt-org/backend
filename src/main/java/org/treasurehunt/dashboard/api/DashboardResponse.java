package org.treasurehunt.dashboard.api;

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
public class DashboardResponse {
    private List<DashboardItem> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardItem {
        private String id;
        private String title;
        private double progress;
        private int completedChallenges;
        private int totalChallenges;
        private LocalDate dueDate;
    }
}