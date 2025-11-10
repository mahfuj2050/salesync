package com.business.salesync.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    // ✅ Total sales today (using grandTotal)
    @Query("SELECT COALESCE(SUM(o.grandTotal), 0) FROM SalesOrder o WHERE o.dateOrdered = :today")
    double totalSalesToday(@Param("today") LocalDate today);

 // ✅ Total sales this week (using grandTotal)
    @Query("SELECT COALESCE(SUM(o.grandTotal), 0) FROM SalesOrder o WHERE o.dateOrdered BETWEEN :startOfWeek AND :endOfWeek")
    double totalSalesThisWeek(@Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);

    // ✅ Total sales this month (using grandTotal)
    @Query("SELECT COALESCE(SUM(o.grandTotal), 0) FROM SalesOrder o WHERE o.dateOrdered BETWEEN :startOfMonth AND :endOfMonth")
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
    
    


    @Query("""
    		SELECT o FROM SalesOrder o
    		WHERE 
    		    (:search IS NULL OR 
    		     LOWER(o.customer.name) LIKE LOWER(CONCAT('%', :search, '%')) OR 
    		     LOWER(o.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')))
    		AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
    		AND (
    		     (:fromDate IS NULL AND :toDate IS NULL)
    		     OR (o.dateOrdered BETWEEN :fromDate AND :toDate)
    		     OR (:fromDate IS NOT NULL AND :toDate IS NULL AND o.dateOrdered >= :fromDate)
    		     OR (:toDate IS NOT NULL AND :fromDate IS NULL AND o.dateOrdered <= :toDate)
    		)
    		""")
    		Page<SalesOrder> findWithFilters(
    		        @Param("search") String search,
    		        @Param("paymentStatus") SalesOrder.PaymentStatus paymentStatus,
    		        @Param("dateRange") String dateRange,
    		        @Param("fromDate") LocalDate fromDate,
    		        @Param("toDate") LocalDate toDate,
    		        Pageable pageable
    		);


    


     @Query("SELECT SUM(o.totalAmount) FROM SalesOrder o")
     BigDecimal calculateTotalRevenue();

     @Query("SELECT SUM(o.amountDue) FROM SalesOrder o WHERE o.amountDue > 0")
     BigDecimal calculateTotalDue();

     @Query("SELECT COUNT(o) FROM SalesOrder o WHERE DATE(o.dateOrdered) = :today")
     long countTodayOrders(@Param("today") LocalDate today);
     
     // Add this method to bypass the @Where filter
     @Query("SELECT o FROM SalesOrder o WHERE o.id = :id")
     Optional<SalesOrder> findByIdIgnoreDeleted(@Param("id") Long id);
     
     Optional<SalesOrder> findByInvoiceNumber(String invoiceNumber);
    
    
    
}
