package com.emily.qatoolkit.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final DeviceRgb SAGE_DARK  = new DeviceRgb(0x4a, 0x66, 0x50);
    private static final DeviceRgb SAGE_MIST  = new DeviceRgb(0xf0, 0xf7, 0xf1);
    private static final DeviceRgb SAGE_PALE  = new DeviceRgb(0xdc, 0xee, 0xde);
    private static final DeviceRgb INK        = new DeviceRgb(0x1e, 0x2a, 0x20);
    private static final DeviceRgb MUTED      = new DeviceRgb(0x7a, 0x94, 0x80);
    private static final DeviceRgb RED        = new DeviceRgb(0xc0, 0x47, 0x3a);
    private static final DeviceRgb AMBER      = new DeviceRgb(0xc8, 0x83, 0x2a);
    private static final String    DATE_FMT   = "yyyy-MM-dd HH:mm";

    // ── Generate a bug report PDF ────────────────────────────────────
    public byte[] generateBugReportPdf(String bugReportsJson, String sessionName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
             Document doc = new Document(pdf, PageSize.A4)) {

            doc.setMargins(50, 50, 50, 50);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont mono    = PdfFontFactory.createFont(StandardFonts.COURIER);

            // Header bar
            addPageHeader(doc, bold, "DEFECT REPORT", sessionName);

            // Meta line
            doc.add(new Paragraph("Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FMT)) +
                    "  |  Author: Emily Squires — QA Lead")
                    .setFont(regular).setFontSize(9).setFontColor(MUTED)
                    .setMarginBottom(20));

            // Parse JSON array and render each bug report
            // Using simple string splitting — no external JSON lib needed at this layer
            // because the frontend sends well-structured JSON
            if (bugReportsJson != null && !bugReportsJson.isBlank()) {
                doc.add(new Paragraph("Bug Reports")
                        .setFont(bold).setFontSize(13).setFontColor(SAGE_DARK)
                        .setMarginBottom(10));

                // Render raw JSON in mono for now — swap in Jackson parsing for richer layout
                doc.add(new Paragraph(prettyPrintJson(bugReportsJson))
                        .setFont(mono).setFontSize(8.5f)
                        .setFontColor(INK)
                        .setBackgroundColor(SAGE_MIST)
                        .setPadding(10)
                        .setMarginBottom(16));
            } else {
                doc.add(new Paragraph("No bug reports in this session.")
                        .setFont(regular).setFontColor(MUTED).setFontSize(10));
            }

            addPageFooter(doc, regular);
        }
        return baos.toByteArray();
    }

    // ── Generate a test plan PDF ──────────────────────────────────────
    public byte[] generateTestPlanPdf(String testCasesJson, String rtmJson, String sessionName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
             Document doc = new Document(pdf, PageSize.A4.rotate())) { // Landscape for wide tables

            doc.setMargins(40, 40, 40, 40);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont mono    = PdfFontFactory.createFont(StandardFonts.COURIER);

            addPageHeader(doc, bold, "TEST PLAN", sessionName);

            doc.add(new Paragraph("Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FMT)) +
                    "  |  Author: Emily Squires — QA Lead")
                    .setFont(regular).setFontSize(9).setFontColor(MUTED)
                    .setMarginBottom(20));

            // Test Cases section
            doc.add(new Paragraph("Test Cases")
                    .setFont(bold).setFontSize(13).setFontColor(SAGE_DARK)
                    .setMarginBottom(10));

            if (testCasesJson != null && !testCasesJson.isBlank()) {
                // Build a formatted table — columns match frontend schema
                Table table = new Table(new float[]{70, 160, 120, 140, 140, 60, 70})
                        .setWidth(UnitValue.createPercentValue(100));

                String[] headers = {"TC ID", "Description", "Preconditions", "Steps", "Expected Result", "Priority", "Status"};
                for (String h : headers) {
                    table.addHeaderCell(new Cell()
                            .add(new Paragraph(h).setFont(bold).setFontSize(8).setFontColor(SAGE_DARK))
                            .setBackgroundColor(SAGE_PALE)
                            .setPadding(5));
                }

                // Placeholder row — full JSON parsing would use Jackson ObjectMapper
                table.addCell(createCell("See JSON data below", regular, 8, 7));
                doc.add(table);
                doc.add(new Paragraph("\nRaw JSON (copy into your test management tool):").setFont(bold).setFontSize(9).setFontColor(MUTED));
                doc.add(new Paragraph(prettyPrintJson(testCasesJson))
                        .setFont(mono).setFontSize(7.5f).setFontColor(INK)
                        .setBackgroundColor(SAGE_MIST).setPadding(8).setMarginBottom(16));
            } else {
                doc.add(new Paragraph("No test cases in this session.").setFont(regular).setFontColor(MUTED).setFontSize(10));
            }

            // RTM section
            if (rtmJson != null && !rtmJson.isBlank()) {
                doc.add(new AreaBreak());
                doc.add(new Paragraph("Requirements Traceability Matrix")
                        .setFont(bold).setFontSize(13).setFontColor(SAGE_DARK).setMarginBottom(10));
                doc.add(new Paragraph(prettyPrintJson(rtmJson))
                        .setFont(mono).setFontSize(7.5f).setFontColor(INK)
                        .setBackgroundColor(SAGE_MIST).setPadding(8));
            }

            addPageFooter(doc, regular);
        }
        return baos.toByteArray();
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private void addPageHeader(Document doc, PdfFont bold, String type, String sessionName) {
        Table header = new Table(new float[]{1, 1}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(6);
        header.addCell(new Cell().add(
                new Paragraph("Emily Squires · QA Toolkit").setFont(bold).setFontSize(14).setFontColor(SAGE_DARK))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPaddingBottom(4));
        header.addCell(new Cell().add(
                new Paragraph(type).setFont(bold).setFontSize(14).setFontColor(SAGE_DARK)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPaddingBottom(4));
        doc.add(header);

        if (sessionName != null && !sessionName.isBlank()) {
            doc.add(new Paragraph("Session: " + sessionName)
                    .setFont(bold).setFontSize(10).setFontColor(INK).setMarginBottom(2));
        }

        // Rule line
        doc.add(new Paragraph("─".repeat(120))
                .setFont(bold).setFontSize(6).setFontColor(SAGE_PALE).setMarginBottom(12));
    }

    private void addPageFooter(Document doc, PdfFont regular) {
        doc.add(new Paragraph("\n\nThis document was generated by the Emily Squires QA Toolkit. " +
                "For questions contact: emily.squires@email.com")
                .setFont(regular).setFontSize(8).setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private Cell createCell(String text, PdfFont font, float fontSize, int colSpan) {
        return new Cell(1, colSpan)
                .add(new Paragraph(text).setFont(font).setFontSize(fontSize))
                .setPadding(4);
    }

    /** Minimal pretty-printer — adds newlines after commas and braces */
    private String prettyPrintJson(String json) {
        if (json == null) return "";
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        for (char c : json.toCharArray()) {
            if (c == '"' ) inString = !inString;
            if (!inString) {
                if (c == '{' || c == '[') { sb.append(c).append('\n'); indent += 2; sb.append(" ".repeat(indent)); continue; }
                if (c == '}' || c == ']') { sb.append('\n'); indent = Math.max(0, indent - 2); sb.append(" ".repeat(indent)).append(c); continue; }
                if (c == ',') { sb.append(c).append('\n').append(" ".repeat(indent)); continue; }
                if (c == ':') { sb.append(c).append(' '); continue; }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
