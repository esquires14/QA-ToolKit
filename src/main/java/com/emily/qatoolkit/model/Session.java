package com.emily.qatoolkit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A Session holds all the data for one saved toolkit workspace.
 * Test cases, bug reports, and RTM data are stored as JSON strings
 * so the schema stays simple while the frontend can evolve freely.
 */
@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable name set by the user, e.g. "Sprint 14 Regression" */
    @Column(nullable = false, length = 200)
    private String name;

    /** Short random token used in share URLs, e.g. /share/abc123xyz */
    @Column(unique = true, nullable = false, length = 32)
    private String shareToken;

    /** JSON array of test case objects */
    @Column(columnDefinition = "TEXT")
    private String testCasesJson;

    /** JSON array of bug report objects */
    @Column(columnDefinition = "TEXT")
    private String bugReportsJson;

    /** JSON object containing RTM requirements, TC IDs, and coverage map */
    @Column(columnDefinition = "TEXT")
    private String rtmJson;

    /** JSON object containing dashboard counts and sprint trend data */
    @Column(columnDefinition = "TEXT")
    private String dashboardJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
