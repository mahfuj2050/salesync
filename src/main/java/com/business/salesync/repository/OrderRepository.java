package com.business.salesync.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.business.salesync.models.SalesOrder;


public interface OrderRepository extends JpaRepository<SalesOrder, Long> {
	
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
    
}
