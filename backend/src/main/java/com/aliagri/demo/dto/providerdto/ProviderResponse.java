package com.aliagri.demo.dto.providerdto;
import java.time.Instant;
public record ProviderResponse(Long id, String name, String phone, Instant createdAt) {}