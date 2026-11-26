package com.aliagri.demo.dto.productdto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
                Long id,
                String name,
                String licenseNumber,
                Long unitId,
                String unitName,
                BigDecimal stockQuantity,
                Long categoryId,
                String categoryName,
                Instant createdAt) {
}