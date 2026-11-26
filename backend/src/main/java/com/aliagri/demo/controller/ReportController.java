package com.aliagri.demo.controller;

import com.aliagri.demo.dto.report.ReportFilterDto;
import com.aliagri.demo.dto.report.ReportRowDto;
import com.aliagri.demo.service.ReportService;
import com.aliagri.demo.service.report.RegistreExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final RegistreExporter excelExporter;
    private final RegistreExporter pdfExporter;

    public ReportController(
            ReportService reportService,
            @Qualifier("excelExporter") RegistreExporter excelExporter,
            @Qualifier("pdfExporter") RegistreExporter pdfExporter) {
        this.reportService = reportService;
        this.excelExporter = excelExporter;
        this.pdfExporter = pdfExporter;
    }

    @GetMapping("/data")
    public List<ReportRowDto> getReportData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Long productId) {
        ReportFilterDto filter = new ReportFilterDto(startDate, endDate, customerId, providerId, productId);
        return reportService.getReport(filter);
    }

    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> downloadExcel() throws Exception {
        return buildResponse(excelExporter.generate(), "registre_aliagri.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> downloadPdf() throws Exception {
        return buildResponse(pdfExporter.generate(), "registre_aliagri.pdf", "application/pdf");
    }

    private ResponseEntity<InputStreamResource> buildResponse(
            ByteArrayInputStream in, String filename, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(in));
    }
}