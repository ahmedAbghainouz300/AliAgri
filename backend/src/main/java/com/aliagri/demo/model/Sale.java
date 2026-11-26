package com.aliagri.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // --- SNAPSHOT FIELDS ---
    @Column(name = "customer_name_snapshot")
    private String customerNameSnapshot;

    @Column(name = "customer_cin_snapshot")
    private String customerCinSnapshot;

    @Column(name = "customer_address_snapshot")
    private String customerAddressSnapshot;

    // Date fields for report accuracy
    @Column(name = "sale_date", nullable = false, updatable = false)
    private LocalDate saleDate;

    @Column(name = "sale_timestamp", nullable = false, updatable = false)
    private Instant saleTimestamp = Instant.now();

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean cancelled = false;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    public Sale(Customer customer) {
        this.customer = customer;
        this.customerNameSnapshot = customer.getName();
        this.customerCinSnapshot = customer.getIdCardNumber();
        this.customerAddressSnapshot = customer.getAddress();
        this.saleDate = LocalDate.now(ZoneId.of("Africa/Casablanca"));
    }

    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
        this.totalAmount = this.totalAmount.add(item.getPrice().multiply(item.getQuantity()));
    }
}