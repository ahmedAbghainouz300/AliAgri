package com.aliagri.demo.dto.report;

import java.time.LocalDate;

public record ReportFilterDto(
        LocalDate startDate,
        LocalDate endDate,
        Long customerId,
        Long providerId,
        Long productId) {
}