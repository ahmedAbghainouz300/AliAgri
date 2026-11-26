package com.aliagri.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    @JsonIgnore
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // --- SNAPSHOT FIELDS ---
    @Column(name = "product_name_snapshot")
    private String productNameSnapshot;

    @Column(name = "license_number_snapshot")
    private String licenseNumberSnapshot;

    @Column(name = "unit_snapshot")
    private String unitSnapshot;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public PurchaseItem(Product product, BigDecimal quantity, BigDecimal price) {
        this.product = product;
        this.productNameSnapshot = product.getName();
        this.licenseNumberSnapshot = product.getLicenseNumber();
        this.unitSnapshot = product.getUnit() != null ? product.getUnit().getName() : null;
        this.quantity = quantity;
        this.price = price;
    }
}