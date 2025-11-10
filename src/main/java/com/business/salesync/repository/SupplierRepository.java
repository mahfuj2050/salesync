package com.business.salesync.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.business.salesync.models.Supplier;


@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    // Search suppliers by name, email, or phone
    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Supplier> findBySearchTerm(@Param("search") String search, Pageable pageable);
    
    // FIXED: Count total products across all suppliers
    @Query("SELECT COUNT(p) FROM Product p WHERE p.supplier IS NOT NULL")
    Long countTotalProducts();
    
    // FIXED: Count products for a specific supplier
    @Query("SELECT COUNT(p) FROM Product p WHERE p.supplier.id = :supplierId")
    Long countProductsBySupplierId(@Param("supplierId") Long supplierId);
}