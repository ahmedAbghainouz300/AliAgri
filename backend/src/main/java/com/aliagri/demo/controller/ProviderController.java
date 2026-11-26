package com.aliagri.demo.controller;

import com.aliagri.demo.dto.providerdto.ProviderRequest;
import com.aliagri.demo.dto.providerdto.ProviderResponse;
import com.aliagri.demo.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {
    private final ProviderService providerService;

    @GetMapping
    public List<ProviderResponse> getAll() { return providerService.getAll(); }

    @GetMapping("/{id}")
    public ProviderResponse getById(@PathVariable Long id) { return providerService.getById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderResponse create(@RequestBody ProviderRequest request) { return providerService.create(request); }

    @PutMapping("/{id}")
    public ProviderResponse update(@PathVariable Long id, @RequestBody ProviderRequest request) { return providerService.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { providerService.delete(id); }
}