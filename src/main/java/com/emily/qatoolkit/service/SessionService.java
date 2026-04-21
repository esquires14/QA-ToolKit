package com.emily.qatoolkit.service;

import com.emily.qatoolkit.dto.SessionDtos.*;
import com.emily.qatoolkit.model.Session;
import com.emily.qatoolkit.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository repo;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── Create a new session ─────────────────────────────────────────
    public SessionResponse create(SaveRequest req) {
        Session s = new Session();
        s.setName(req.getName());
        s.setShareToken(generateToken());
        applyJsonFields(s, req);
        return toResponse(repo.save(s));
    }

    // ── Update existing session ──────────────────────────────────────
    public SessionResponse update(Long id, SaveRequest req) {
        Session s = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + id));
        s.setName(req.getName());
        applyJsonFields(s, req);
        return toResponse(repo.save(s));
    }

    // ── Load by ID ───────────────────────────────────────────────────
    public SessionResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + id)));
    }

    // ── Load by share token (public, read-only view) ─────────────────
    public SessionResponse getByToken(String token) {
        return toResponse(repo.findByShareToken(token)
                .orElseThrow(() -> new NoSuchElementException("Session not found")));
    }

    // ── List all sessions (summary only) ────────────────────────────
    public List<SessionSummary> listAll() {
        return repo.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    // ── Delete ───────────────────────────────────────────────────────
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("Session not found: " + id);
        }
        repo.deleteById(id);
    }

    // ── Get share link for existing session ──────────────────────────
    public ShareLinkResponse getShareLink(Long id) {
        Session s = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + id));
        ShareLinkResponse r = new ShareLinkResponse();
        r.setShareToken(s.getShareToken());
        r.setShareUrl(shareUrl(s.getShareToken()));
        return r;
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private void applyJsonFields(Session s, SaveRequest req) {
        if (req.getTestCasesJson()  != null) s.setTestCasesJson(req.getTestCasesJson());
        if (req.getBugReportsJson() != null) s.setBugReportsJson(req.getBugReportsJson());
        if (req.getRtmJson()        != null) s.setRtmJson(req.getRtmJson());
        if (req.getDashboardJson()  != null) s.setDashboardJson(req.getDashboardJson());
    }

    private String generateToken() {
        SecureRandom rng = new SecureRandom();
        byte[] bytes = new byte[18];
        rng.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // Retry if collision (astronomically unlikely, but correct)
        if (repo.existsByShareToken(token)) return generateToken();
        return token;
    }

    private String shareUrl(String token) {
        return baseUrl + "/share/" + token;
    }

    private SessionResponse toResponse(Session s) {
        SessionResponse r = new SessionResponse();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setShareToken(s.getShareToken());
        r.setShareUrl(shareUrl(s.getShareToken()));
        r.setTestCasesJson(s.getTestCasesJson());
        r.setBugReportsJson(s.getBugReportsJson());
        r.setRtmJson(s.getRtmJson());
        r.setDashboardJson(s.getDashboardJson());
        r.setCreatedAt(s.getCreatedAt().format(FMT));
        r.setUpdatedAt(s.getUpdatedAt().format(FMT));
        return r;
    }

    private SessionSummary toSummary(Session s) {
        SessionSummary r = new SessionSummary();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setShareToken(s.getShareToken());
        r.setShareUrl(shareUrl(s.getShareToken()));
        r.setCreatedAt(s.getCreatedAt().format(FMT));
        r.setUpdatedAt(s.getUpdatedAt().format(FMT));
        return r;
    }
}
