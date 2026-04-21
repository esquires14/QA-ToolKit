package com.emily.qatoolkit.controller;

import com.emily.qatoolkit.dto.SessionDtos.*;
import com.emily.qatoolkit.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService service;

    /**
     * POST /api/sessions
     * Create and save a new session. Returns the saved session with its share token.
     */
    @PostMapping
    public ResponseEntity<SessionResponse> create(@Valid @RequestBody SaveRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    /**
     * PUT /api/sessions/{id}
     * Overwrite an existing session's data (e.g. auto-save on tab change).
     */
    @PutMapping("/{id}")
    public ResponseEntity<SessionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SaveRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    /**
     * GET /api/sessions
     * List all saved sessions (summaries only — no JSON payloads).
     */
    @GetMapping
    public ResponseEntity<List<SessionSummary>> list() {
        return ResponseEntity.ok(service.listAll());
    }

    /**
     * GET /api/sessions/{id}
     * Load a full session by its numeric ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * GET /api/sessions/share/{token}
     * Load a session by its public share token (no auth required).
     */
    @GetMapping("/share/{token}")
    public ResponseEntity<SessionResponse> getByToken(@PathVariable String token) {
        return ResponseEntity.ok(service.getByToken(token));
    }

    /**
     * GET /api/sessions/{id}/share-link
     * Return just the share URL for a session.
     */
    @GetMapping("/{id}/share-link")
    public ResponseEntity<ShareLinkResponse> getShareLink(@PathVariable Long id) {
        return ResponseEntity.ok(service.getShareLink(id));
    }

    /**
     * DELETE /api/sessions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Error handling ───────────────────────────────────────────────
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
