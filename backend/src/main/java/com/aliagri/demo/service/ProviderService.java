package com.aliagri.demo.service;

import com.aliagri.demo.dto.providerdto.ProviderRequest;
import com.aliagri.demo.dto.providerdto.ProviderResponse;
import com.aliagri.demo.model.Provider;
import com.aliagri.demo.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;

    public List<ProviderResponse> getAll() {
        return providerRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public ProviderResponse getById(Long id) {
        return mapToResponse(requireProvider(id));
    }

    public ProviderResponse create(ProviderRequest request) {
        Provider provider = new Provider(request.name(), request.phone());
        return mapToResponse(providerRepository.save(provider));
    }

    public ProviderResponse update(Long id, ProviderRequest request) {
        Provider provider = requireProvider(id);
        provider.setName(request.name());
        provider.setPhone(request.phone());
        return mapToResponse(providerRepository.save(provider));
    }

    public void delete(Long id) {
        providerRepository.delete(requireProvider(id));
    }

    private Provider requireProvider(Long id) {
        return providerRepository.findById(id).orElseThrow(() -> new RuntimeException("Provider not found"));
    }

    private ProviderResponse mapToResponse(Provider provider) {
        return new ProviderResponse(provider.getId(), provider.getName(), provider.getPhone(), provider.getCreatedAt());
    }
}