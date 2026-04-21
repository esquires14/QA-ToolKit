package com.emily.qatoolkit.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ── Request: create or update a session ──────────────────────────────
public class SessionDtos {

    @Data
    public static class SaveRequest {
        @NotBlank(message = "Session name is required")
        @Size(max = 200, message = "Name must be 200 characters or fewer")
        private String name;

        private String testCasesJson;
        private String bugReportsJson;
        private String rtmJson;
        private String dashboardJson;
    }

    // ── Response: full session data ───────────────────────────────────
    @Data
    public static class SessionResponse {
        private Long id;
        private String name;
        private String shareToken;
        private String shareUrl;
        private String testCasesJson;
        private String bugReportsJson;
        private String rtmJson;
        private String dashboardJson;
        private String createdAt;
        private String updatedAt;
    }

    // ── Response: lightweight summary for session list ────────────────
    @Data
    public static class SessionSummary {
        private Long id;
        private String name;
        private String shareToken;
        private String shareUrl;
        private String createdAt;
        private String updatedAt;
    }

    // ── Response: share link only ─────────────────────────────────────
    @Data
    public static class ShareLinkResponse {
        private String shareToken;
        private String shareUrl;
    }
}
