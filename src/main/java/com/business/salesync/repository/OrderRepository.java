package com.business.salesync.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
//✅ CORRECT IMPORT - Use this instead
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.business.salesync.models.SalesOrder;


public interface OrderRepository extends JpaRepository<SalesOrder, Long> {
	
	List<SalesOrder> findByCustomerId(Long customerId);
	
	  // Fetch all orders between two dates
    List<SalesOrder> findByDateOrderedBetween(LocalDate startDate, LocalDate endDate);

    // Total sales today
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM SalesOrder o WHERE o.dateOrdered = :today")
    double totalSalesToday(@Param("today") LocalDate today);

    // Total sales this week
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM SalesOrder o WHERE o.dateOrdered BETWEEN :startOfWeek AND :endOfWeek")
    double totalSalesThisWeek(@Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);

    // Total sales this month
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM SalesOrder o WHERE o.dateOrdered BETWEEN :startOfMonth AND :endOfMonth")
    double totalSalesThisMonth(@Param("startOfMonth") LocalDate startOfMonth, @Param("endOfMonth") LocalDate endOfMonth);

    // Find only non-deleted orders
    List<SalesOrder> findByDeletedFalse();
    
    // Find only deleted orders
    List<SalesOrder> findByDeletedTrue();
    
    // Soft delete by ID
    @Modifying
    @Query("UPDATE SalesOrder o SET o.deleted = true WHERE o.id = :id")
    void softDeleteById(@Param("id") Long id);
    
    // Restore by ID
    @Modifying
    @Query("UPDATE SalesOrder o SET o.deleted = false WHERE o.id = :id")
    void restoreById(@Param("id") Long id);
    
    
    
    @Query("SELECT o FROM SalesOrder o WHERE " +
            "(:search IS NULL OR o.invoiceNumber LIKE %:search% OR o.customer.name LIKE %:search%) AND " +
            "(:paymentStatus IS NULL OR " +
            "  (:paymentStatus = 'paid' AND o.amountDue = 0) OR " +
            "  (:paymentStatus = 'partial' AND o.amountDue > 0 AND o.amountPaid > 0) OR " +
            "  (:paymentStatus = 'unpaid' AND o.amountPaid = 0)) AND " +
            "(:fromDate IS NULL OR o.dateOrdered >= :fromDate) AND " +
            "(:toDate IS NULL OR o.dateOrdered <= :toDate)")
     Page<SalesOrder> findWithFilters(
         @Param("search") String search,
         @Param("paymentStatus") String paymentStatus,
         @Param("dateRange") String dateRange,
         @Param("fromDate") LocalDate fromDate,
         @Param("toDate") LocalDate toDate,
         Pageable pageable);

     @Query("SELECT SUM(o.totalAmount) FROM SalesOrder o")
     BigDecimal calculateTotalRevenue();

     @Query("SELECT SUM(o.amountDue) FROM SalesOrder o WHERE o.amountDue > 0")
     BigDecimal calculateTotalDue();

     @Query("SELECT COUNT(o) FROM SalesOrder o WHERE DATE(o.dateOrdered) = :today")
     long countTodayOrders(@Param("today") LocalDate today);
    
    
    
}
