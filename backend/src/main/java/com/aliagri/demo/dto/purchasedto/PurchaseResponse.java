package com.aliagri.demo.dto.purchasedto;

import com.aliagri.demo.dto.providerdto.ProviderResponse;

import java.math.BigDecimal;
import java.time.LocalDate; // Changed from Instant
import java.util.List;

public record PurchaseResponse(
                Long id,
                LocalDate purchaseDate, // Changed from Instant
                BigDecimal totalAmount,
                ProviderResponse provider,
                List<PurchaseItemDto> items) {
        public record PurchaseItemDto(
                        Long id,
                        String productName,
                        String licenseNumber,
                        BigDecimal quantity,
                        BigDecimal price) {
        }
}