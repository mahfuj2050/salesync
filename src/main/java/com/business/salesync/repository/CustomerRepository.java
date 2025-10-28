package com.business.salesync.repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.business.salesync.models.Customer;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 👥 Customer Repository
 * Handles all database operations for Customer entity
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // ========================================
    // 🔍 BASIC QUERIES
    // ========================================

    /**
     * Find customer by phone number
     */
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find customers by name (case-insensitive partial match)
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find last customer (for generating next ID)
     */
    Optional<Customer> findTopByOrderByIdDesc();

    // ========================================
    // 🔍 SEARCH QUERIES
    // ========================================

    /**
     * Search customers by multiple criteria
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:phone IS NULL OR c.phoneNumber LIKE CONCAT('%', :phone, '%')) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:address IS NULL OR LOWER(c.address) LIKE LOWER(CONCAT('%', :address, '%')))")
    List<Customer> searchCustomers(
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("address") String address
    );

    /**
     * Search by keyword (searches in name, phone, email, address)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "c.phoneNumber LIKE CONCAT('%', :keyword, '%') OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchByKeyword(@Param("keyword") String keyword);

    // ========================================
    // 📊 ANALYTICS
    // ========================================

    /**
     * Count total customers
     */
    @Query("SELECT COUNT(c) FROM Customer c")
    long countTotalCustomers();

    /**
     * Get all customers ordered by name
     */
    List<Customer> findAllByOrderByNameAsc();

    /**
     * Get recent customers (last N)
     */
    @Query("SELECT c FROM Customer c ORDER BY c.id DESC")
    List<Customer> findRecentCustomers();
}