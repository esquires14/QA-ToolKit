package com.emily.qatoolkit.service;

import com.emily.qatoolkit.dto.SessionDtos.*;
import com.emily.qatoolkit.model.Session;
import com.emily.qatoolkit.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock  private SessionRepository repo;
    @InjectMocks private SessionService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
    }

    // ── Helper ──
    private Session buildSession(Long id, String name, String token) {
        Session s = new Session();
        s.setId(id);
        s.setName(name);
        s.setShareToken(token);
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        return s;
    }

    @Test
    @DisplayName("create() persists a new session and returns a response with share URL")
    void create_persistsAndReturnsShareUrl() {
        SaveRequest req = new SaveRequest();
        req.setName("Sprint 14 Regression");
        req.setTestCasesJson("[{\"id\":\"TC-001\"}]");

        Session saved = buildSession(1L, "Sprint 14 Regression", "abc123");
        saved.setTestCasesJson(req.getTestCasesJson());

        when(repo.existsByShareToken(anyString())).thenReturn(false);
        when(repo.save(any(Session.class))).thenReturn(saved);

        SessionResponse resp = service.create(req);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getName()).isEqualTo("Sprint 14 Regression");
        assertThat(resp.getShareUrl()).startsWith("http://localhost:8080/share/");
        verify(repo, times(1)).save(any(Session.class));
    }

    @Test
    @DisplayName("getById() returns session when it exists")
    void getById_returnsSession() {
        Session s = buildSession(2L, "Smoke Test", "tok999");
        when(repo.findById(2L)).thenReturn(Optional.of(s));

        SessionResponse resp = service.getById(2L);

        assertThat(resp.getName()).isEqualTo("Smoke Test");
    }

    @Test
    @DisplayName("getById() throws NoSuchElementException when not found")
    void getById_throwsWhenMissing() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getByToken() returns session for valid token")
    void getByToken_returnsSession() {
        Session s = buildSession(3L, "UAT Run", "shareABC");
        when(repo.findByShareToken("shareABC")).thenReturn(Optional.of(s));

        SessionResponse resp = service.getByToken("shareABC");

        assertThat(resp.getShareToken()).isEqualTo("shareABC");
    }

    @Test
    @DisplayName("delete() calls repository deleteById")
    void delete_callsRepo() {
        when(repo.existsById(5L)).thenReturn(true);
        doNothing().when(repo).deleteById(5L);

        service.delete(5L);

        verify(repo, times(1)).deleteById(5L);
    }

    @Test
    @DisplayName("listAll() maps sessions to summaries")
    void listAll_returnsSummaries() {
        when(repo.findAll()).thenReturn(List.of(
                buildSession(1L, "A", "t1"),
                buildSession(2L, "B", "t2")
        ));

        List<SessionSummary> list = service.listAll();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getName()).isEqualTo("A");
        assertThat(list.get(1).getName()).isEqualTo("B");
    }
}
