package com.aliagri.demo.service;

import com.aliagri.demo.dto.providerdto.ProviderResponse;
import com.aliagri.demo.dto.purchasedto.PurchaseRequest;
import com.aliagri.demo.dto.purchasedto.PurchaseResponse;
import com.aliagri.demo.model.Product;
import com.aliagri.demo.model.Provider;
import com.aliagri.demo.model.Purchase;
import com.aliagri.demo.model.PurchaseItem;
import com.aliagri.demo.repository.ProductRepository;
import com.aliagri.demo.repository.ProviderRepository;
import com.aliagri.demo.repository.PurchaseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProviderRepository providerRepository;
    private final ProductRepository productRepository;

    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest request) {
        Provider provider = providerRepository.findById(request.providerId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        Purchase purchase = new Purchase(provider);

        for (PurchaseRequest.PurchaseItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // INCREASE STOCK
            product.setStockQuantity(product.getStockQuantity().add(itemReq.quantity()));
            productRepository.save(product);

            PurchaseItem purchaseItem = new PurchaseItem(product, itemReq.quantity(), itemReq.price());
            purchase.addItem(purchaseItem);
        }

        Purchase savedPurchase = purchaseRepository.save(purchase);
        return mapToResponse(savedPurchase);
    }

    @Transactional
    public List<PurchaseResponse> getAllPurchases() {
        return purchaseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));
        // Note: For a real app, you might want to decrease stock back if a purchase is deleted.
        // We will keep it simple for now.
        purchaseRepository.delete(purchase);
    }

    private PurchaseResponse mapToResponse(Purchase purchase) {
        ProviderResponse providerDto = new ProviderResponse(
                purchase.getProvider().getId(),
                purchase.getProvider().getName(),
                purchase.getProvider().getPhone(),
                purchase.getProvider().getCreatedAt()
        );

        List<PurchaseResponse.PurchaseItemDto> itemDtos = purchase.getItems().stream()
                .map(item -> new PurchaseResponse.PurchaseItemDto(
                        item.getId(),
                        item.getProduct().getName(),
                        item.getProduct().getLicenseNumber(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());

        return new PurchaseResponse(
                purchase.getId(),
                purchase.getPurchaseDate(),
                purchase.getTotalAmount(),
                providerDto,
                itemDtos
        );
    }
}