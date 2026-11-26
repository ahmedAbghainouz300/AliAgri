package com.aliagri.demo.repository;

import com.aliagri.demo.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("SELECT DISTINCT s FROM Sale s " +
            "JOIN FETCH s.customer " +
            "JOIN FETCH s.items i " +
            "JOIN FETCH i.product " +
            "WHERE (CAST(:startDate AS date) IS NULL OR s.saleDate >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR s.saleDate <= :endDate) " +
            "AND (CAST(:customerId AS long) IS NULL OR s.customer.id = :customerId) " +
            "AND (CAST(:productId AS long) IS NULL OR i.product.id = :productId)")
    List<Sale> findFilteredSales(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("customerId") Long customerId,
            @Param("productId") Long productId);

    @Query("SELECT DISTINCT s FROM Sale s JOIN FETCH s.customer JOIN FETCH s.items i JOIN FETCH i.product")
    List<Sale> findAllWithDetails();
}