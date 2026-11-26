package com.aliagri.demo.dto.productdto;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String licenseNumber,
        Long unitId,
        BigDecimal stockQuantity,
        Long categoryId) {
}