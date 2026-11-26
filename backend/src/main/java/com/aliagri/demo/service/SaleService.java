package com.aliagri.demo.service;

import com.aliagri.demo.dto.customerdto.CustomerResponse;
import com.aliagri.demo.dto.saledto.SaleRequest;
import com.aliagri.demo.dto.saledto.SaleResponse;
import com.aliagri.demo.model.Customer;
import com.aliagri.demo.model.Product;
import com.aliagri.demo.model.Sale;
import com.aliagri.demo.model.SaleItem;
import com.aliagri.demo.repository.CustomerRepository;
import com.aliagri.demo.repository.ProductRepository;
import com.aliagri.demo.repository.SaleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

        private final SaleRepository saleRepository;
        private final CustomerRepository customerRepository;
        private final ProductRepository productRepository;

        @Transactional
        public SaleResponse createSale(SaleRequest request) {
                Customer customer = customerRepository.findById(request.customerId())
                                .orElseThrow(() -> new RuntimeException("Customer not found"));

                Sale sale = new Sale(customer);

                for (SaleRequest.SaleItemRequest itemReq : request.items()) {
                        Product product = productRepository.findById(itemReq.productId())
                                        .orElseThrow(() -> new RuntimeException("Product not found"));

                        if (!product.isActive()) {
                                throw new RuntimeException("Product is archived and cannot be sold.");
                        }

                        if (product.getStockQuantity().compareTo(itemReq.quantity()) < 0) {
                                throw new RuntimeException("Not enough stock for product: " + product.getName());
                        }

                        // Deduct stock
                        product.setStockQuantity(product.getStockQuantity().subtract(itemReq.quantity()));
                        productRepository.save(product);

                        // Add item (Snapshot happens inside the constructor)
                        SaleItem saleItem = new SaleItem(product, itemReq.quantity(), itemReq.price());
                        sale.addItem(saleItem);
                }

                Sale savedSale = saleRepository.save(sale);
                return mapToResponse(savedSale);
        }

        @Transactional
        public List<SaleResponse> getAllSales() {
                return saleRepository.findAllWithDetails().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Transactional
        public SaleResponse getSaleById(Long id) {
                Sale sale = saleRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Sale not found"));
                return mapToResponse(sale);
        }

        @Transactional
        public void deleteSale(Long id) {
                Sale sale = saleRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Sale not found"));
                saleRepository.delete(sale);
        }

        // --- The Mapper Method ---
        private SaleResponse mapToResponse(Sale sale) {
                CustomerResponse customerResponse = new CustomerResponse(
                                sale.getCustomer().getId(),
                                sale.getCustomer().getName(),
                                sale.getCustomer().getIdCardNumber(),
                                sale.getCustomer().getAddress());

                List<SaleResponse.SaleItemDto> itemDtos = sale.getItems().stream()
                                .map(item -> new SaleResponse.SaleItemDto(
                                                item.getId(),
                                                item.getProductNameSnapshot(), // Use snapshot
                                                item.getLicenseNumberSnapshot(), // Use snapshot
                                                item.getQuantity(),
                                                item.getPrice()))
                                .collect(Collectors.toList());

                return new SaleResponse(
                                sale.getId(),
                                sale.getSaleDate(),
                                sale.getTotalAmount(),
                                customerResponse,
                                itemDtos);
        }
}