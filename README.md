# QA Toolkit — Spring Boot Backend

**Author:** Emily Squires
**Stack:** Java 17 · Spring Boot 3.2 · JPA · H2 (dev) / PostgreSQL (prod) · iText7

---

## What this does

REST API backing the QA Toolkit frontend. Three features:

| Feature | Endpoints |
|---|---|
| **Save & load sessions** | `POST /api/sessions`, `GET /api/sessions/{id}`, `PUT /api/sessions/{id}` |
| **Shareable links** | `GET /api/sessions/{id}/share-link`, `GET /api/sessions/share/{token}` |
| **PDF export** | `GET /api/export/{id}/bug-report`, `GET /api/export/{id}/test-plan` |

---

## Run locally (zero config)

```bash
# Clone and run — uses H2 in-memory DB by default
./mvnw spring-boot:run

# App starts at http://localhost:8080
# H2 console at http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:qatoolkit)
```

---

## API Reference

### Sessions

```
POST   /api/sessions              Create a new session
GET    /api/sessions              List all sessions (summaries)
GET    /api/sessions/{id}         Load full session
PUT    /api/sessions/{id}         Update session
DELETE /api/sessions/{id}         Delete session
GET    /api/sessions/{id}/share-link    Get shareable URL
GET    /api/sessions/share/{token}      Load session by share token (public)
```

**POST /api/sessions — request body:**
```json
{
  "name": "Sprint 14 Regression",
  "testCasesJson": "[{\"id\":\"TC-001\",\"desc\":\"...\"}]",
  "bugReportsJson": "[{\"id\":\"BUG-1234\",\"summary\":\"...\"}]",
  "rtmJson": "{\"reqs\":[...],\"tcs\":[...],\"map\":{}}",
  "dashboardJson": "{\"pass\":42,\"fail\":9}"
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Sprint 14 Regression",
  "shareToken": "aB3xZ9_mQr2wLk8P",
  "shareUrl": "http://localhost:8080/share/aB3xZ9_mQr2wLk8P",
  "testCasesJson": "...",
  "createdAt": "2025-03-24 14:30:00",
  "updatedAt": "2025-03-24 14:30:00"
}
```

### PDF Export

```
GET /api/export/{sessionId}/bug-report    → downloads bug-report-{id}.pdf
GET /api/export/{sessionId}/test-plan     → downloads test-plan-{id}.pdf
```

### Health

```
GET /api/health    → {"status":"UP", "service":"QA Toolkit API", ...}
```

---

## Deploy to Render (alternative)

1. New Web Service → connect GitHub repo
2. Build command: `./mvnw clean package -DskipTests`
3. Start command: `java -jar target/qa-toolkit-1.0.0.jar`
4. Add a PostgreSQL database and set the same env vars as above

---

## Project structure

```
src/
├── main/java/com/emily/qatoolkit/
│   ├── QaToolkitApplication.java       Entry point
│   ├── WebConfig.java                  CORS config
│   ├── controller/
│   │   ├── SessionController.java      Save/load/share endpoints
│   │   ├── ExportController.java       PDF download endpoints
│   │   └── HealthController.java       Health check
│   ├── service/
│   │   ├── SessionService.java         Business logic
│   │   └── PdfService.java             iText7 PDF generation
│   ├── model/
│   │   └── Session.java                JPA entity
│   ├── repository/
│   │   └── SessionRepository.java      Spring Data JPA
│   └── dto/
│       └── SessionDtos.java            Request/response objects
└── test/java/com/emily/qatoolkit/
    └── service/
        └── SessionServiceTest.java     Unit tests (Mockito)
```

---

## Frontend integration

In the frontend HTML, point API calls to your backend:

```javascript
const API = 'http://localhost:8080/api'; // swap to your Railway URL in prod

// Save session
const res = await fetch(`${API}/sessions`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: 'Sprint 14',
    testCasesJson: JSON.stringify(tcRows),
    bugReportsJson: JSON.stringify(bugReports),
    rtmJson: JSON.stringify({ reqs: rtmReqs, tcs: rtmTCs, map: rtmMap }),
    dashboardJson: JSON.stringify(dashData)
  })
});
const session = await res.json();
console.log('Share URL:', session.shareUrl);

// Load session by share token
const shared = await fetch(`${API}/sessions/share/${token}`).then(r => r.json());

// Download PDF
window.open(`${API}/export/${session.id}/bug-report`);
```

---

## Running tests

```bash
./mvnw test
```

5 unit tests covering: create, getById (found + not found), getByToken, delete, listAll.
