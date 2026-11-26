package com.aliagri.demo.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReportRowDto(
        Long id,
        String type, // "VENTE" or "ACHAT"
        LocalDate date,
        String partyName, // Customer Name or Provider Name
        String partyId, // CIN for customer, Phone for provider
        String productName,
        String licenseNumber,
        String unit,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal totalAmount) {
}