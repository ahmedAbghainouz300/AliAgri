package com.aliagri.demo.dto.purchasedto;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseRequest(
        Long providerId,
        List<PurchaseItemRequest> items
) {
    public record PurchaseItemRequest(
            Long productId,
            BigDecimal quantity,
            BigDecimal price
    ) {}
}