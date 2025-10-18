package com.business.salesync.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.Brand;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    // Search brands by name (case insensitive)
    Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Find brand by exact name (case insensitive) for duplicate checking
    Optional<Brand> findByNameIgnoreCase(String name);
    
    // CORRECTED: Count total products across all brands
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand IS NOT NULL")
    Long countTotalProducts();
    
    // Alternative: Count products for a specific brand
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId")
    Long countProductsByBrandId(@Param("brandId") Long brandId);
}