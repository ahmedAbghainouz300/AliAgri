package com.aliagri.demo.service;

import com.aliagri.demo.dto.productdto.ProductRequest;
import com.aliagri.demo.dto.productdto.ProductResponse;
import com.aliagri.demo.model.Category;
import com.aliagri.demo.model.Product;
import com.aliagri.demo.model.Unit;
import com.aliagri.demo.repository.CategoryRepository;
import com.aliagri.demo.repository.ProductRepository;
import com.aliagri.demo.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public ProductResponse getById(Long id) {
        return mapToResponse(requireProduct(id));
    }

    public ProductResponse create(ProductRequest request) {
        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new RuntimeException("Unit not found"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product(request.name(), request.licenseNumber(), unit, request.stockQuantity(), category);
        return mapToResponse(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = requireProduct(id);
        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new RuntimeException("Unit not found"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setName(request.name());
        product.setLicenseNumber(request.licenseNumber());
        product.setUnit(unit);
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(category);

        return mapToResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = requireProduct(id);
        // Prevent hard delete if we want to keep history (Phase 1 requirement)
        product.setActive(false);
        productRepository.save(product);
    }

    private Product requireProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getLicenseNumber(),
                product.getUnit() != null ? product.getUnit().getId() : null,
                product.getUnit() != null ? product.getUnit().getName() : null,
                product.getStockQuantity(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCreatedAt());
    }
}