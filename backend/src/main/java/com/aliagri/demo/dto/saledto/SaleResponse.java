package com.aliagri.demo.dto.saledto;

import com.aliagri.demo.dto.customerdto.CustomerResponse;

import java.math.BigDecimal;
import java.time.LocalDate; // Changed from Instant
import java.util.List;

public record SaleResponse(
                Long id,
                LocalDate saleDate, // Changed from Instant
                BigDecimal totalAmount,
                CustomerResponse customer,
                List<SaleItemDto> items) {
        public record SaleItemDto(
                        Long id,
                        String productName,
                        String licenseNumber,
                        BigDecimal quantity,
                        BigDecimal price) {
        }
}