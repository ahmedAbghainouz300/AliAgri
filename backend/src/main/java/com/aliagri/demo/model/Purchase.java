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
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    // --- SNAPSHOT FIELDS ---
    @Column(name = "provider_name_snapshot")
    private String providerNameSnapshot;

    @Column(name = "purchase_date", nullable = false, updatable = false)
    private LocalDate purchaseDate;

    @Column(name = "purchase_timestamp", nullable = false, updatable = false)
    private Instant purchaseTimestamp = Instant.now();

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseItem> items = new ArrayList<>();

    public Purchase(Provider provider) {
        this.provider = provider;
        this.providerNameSnapshot = provider.getName();
        this.purchaseDate = LocalDate.now(ZoneId.of("Africa/Casablanca"));
    }

    public void addItem(PurchaseItem item) {
        items.add(item);
        item.setPurchase(this);
        this.totalAmount = this.totalAmount.add(item.getPrice().multiply(item.getQuantity()));
    }
}