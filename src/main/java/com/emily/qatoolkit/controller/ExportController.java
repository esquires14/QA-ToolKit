package com.emily.qatoolkit.controller;

import com.emily.qatoolkit.dto.SessionDtos.SessionResponse;
import com.emily.qatoolkit.service.PdfService;
import com.emily.qatoolkit.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final PdfService pdfService;
    private final SessionService sessionService;

    /**
     * GET /api/export/{sessionId}/bug-report
     * Download a PDF of all bug reports in a session.
     */
    @GetMapping("/{sessionId}/bug-report")
    public ResponseEntity<byte[]> exportBugReport(@PathVariable Long sessionId) throws Exception {
        SessionResponse session = sessionService.getById(sessionId);
        byte[] pdf = pdfService.generateBugReportPdf(session.getBugReportsJson(), session.getName());
        return buildPdfResponse(pdf, "bug-report-" + sessionId + ".pdf");
    }

    /**
     * GET /api/export/{sessionId}/test-plan
     * Download a PDF of all test cases + RTM in a session.
     */
    @GetMapping("/{sessionId}/test-plan")
    public ResponseEntity<byte[]> exportTestPlan(@PathVariable Long sessionId) throws Exception {
        SessionResponse session = sessionService.getById(sessionId);
        byte[] pdf = pdfService.generateTestPlanPdf(
                session.getTestCasesJson(),
                session.getRtmJson(),
                session.getName()
        );
        return buildPdfResponse(pdf, "test-plan-" + sessionId + ".pdf");
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .body(pdf);
    }
}
