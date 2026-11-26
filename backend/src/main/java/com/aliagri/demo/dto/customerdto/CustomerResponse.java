package com.aliagri.demo.dto.customerdto;

public record CustomerResponse(
        Long id,
        String name,
        String idCardNumber,
        String address
) {}