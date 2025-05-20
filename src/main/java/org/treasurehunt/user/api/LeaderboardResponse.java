package org.treasurehunt.user.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response class for the leaderboard endpoint.
 * Contains a list of users with their scores, sorted by score in descending order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponse {
    private List<LeaderboardUser> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number; // current page (0-based)

    /**
     * Represents a user entry in the leaderboard.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardUser {
        private Long id;
        private String username;
        private Integer points;
        private String profileImage;
    }
}