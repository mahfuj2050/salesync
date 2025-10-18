package com.business.salesync.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.business.salesync.models.Product;


public interface ProductRepository extends JpaRepository<Product, Long> {

    public List<Product> findByName(String name);

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity + :quantity WHERE p.id = :id")
    void updateQuantity(@Param(value = "id") long id, @Param(value = "quantity") int quantity);
    
    // FIXED: Simplified query to avoid ONLY_FULL_GROUP_BY issues
    @Query("SELECT p FROM Product p WHERE " +
           "(:search IS NULL OR :search = '' OR p.name LIKE %:search% OR p.sku LIKE %:search% OR p.barcode LIKE %:search%) AND " +
           "(:category IS NULL OR p.category.id = :category) AND " +
           "(:brand IS NULL OR p.brand.id = :brand) AND " +
           "(:stockStatus IS NULL OR :stockStatus = '' OR " +
           " (CASE WHEN :stockStatus = 'low' THEN (p.quantity <= p.minStockLevel AND p.quantity > 0) " +
           "       WHEN :stockStatus = 'out' THEN (p.quantity = 0) " +
           "       WHEN :stockStatus = 'in' THEN (p.quantity > 0) " +
           "       ELSE true END))")
    Page<Product> findWithFilters(@Param("search") String search,
                                  @Param("category") Long category,
                                  @Param("brand") Long brand,
                                  @Param("stockStatus") String stockStatus,
                                  Pageable pageable);
     
    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity <= p.minStockLevel AND p.quantity > 0")
    long countLowStockProducts();
     
    @Query("SELECT SUM(p.sellingPrice * p.quantity) FROM Product p WHERE p.quantity > 0")
    BigDecimal calculateTotalStockValue();
}
