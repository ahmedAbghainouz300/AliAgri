package com.aliagri.demo.dto.saledto;

import java.math.BigDecimal;
import java.util.List;

public record SaleRequest(
        Long customerId,
        List<SaleItemRequest> items
) {
    public record SaleItemRequest(
            Long productId,
            BigDecimal quantity,
            BigDecimal price
    ) {}
}