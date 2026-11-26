package com.aliagri.demo.repository;

import com.aliagri.demo.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("SELECT DISTINCT p FROM Purchase p " +
            "JOIN FETCH p.provider " +
            "JOIN FETCH p.items i " +
            "JOIN FETCH i.product " +
            "WHERE (CAST(:startDate AS date) IS NULL OR p.purchaseDate >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR p.purchaseDate <= :endDate) " +
            "AND (CAST(:providerId AS long) IS NULL OR p.provider.id = :providerId) " +
            "AND (CAST(:productId AS long) IS NULL OR i.product.id = :productId)")
    List<Purchase> findFilteredPurchases(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("providerId") Long providerId,
            @Param("productId") Long productId);

    @Query("SELECT DISTINCT p FROM Purchase p JOIN FETCH p.provider JOIN FETCH p.items i JOIN FETCH i.product")
    List<Purchase> findAllWithDetails();
}