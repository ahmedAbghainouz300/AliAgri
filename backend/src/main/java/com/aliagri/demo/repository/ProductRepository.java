package com.aliagri.demo.repository;

import com.aliagri.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring Data JPA will automatically generate the SQL queries for basic CRUD operations.
}