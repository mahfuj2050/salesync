package com.business.salesync.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.Payment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payments by reference type
    Page<Payment> findByRefType(Payment.RefType refType, Pageable pageable);
    
    // Find payments by payment status
    Page<Payment> findByPaymentStatus(Payment.PaymentStatus paymentStatus, Pageable pageable);
    
    // Find payments by payment method
    Page<Payment> findByMethod(String method, Pageable pageable);
    
    // Find payments within date range
    Page<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Search payments by fromAccount or toAccount
    @Query("SELECT p FROM Payment p WHERE p.fromAccount LIKE %:searchTerm% OR p.toAccount LIKE %:searchTerm%")
    Page<Payment> searchByAccount(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find payments by reference ID and type
    List<Payment> findByRefIdAndRefType(Long refId, Payment.RefType refType);
    
    // Get total received amount for orders
    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.refType = 'ORDER' AND p.paymentStatus IN ('PAID', 'PARTIALLY_PAID')")
    Double getTotalReceivedAmount();
    
    // Get total paid amount for purchases and expenses
    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.refType IN ('PURCHASE_ORDER', 'EXPENSE') AND p.paymentStatus IN ('PAID', 'PARTIALLY_PAID')")
    Double getTotalPaidAmount();
    
    @Query("SELECT COALESCE(SUM(p.amountDue), 0) FROM Payment p")
    Double getTotalAmountDueAll();
    
    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p")
    Double getTotalAmountPaidAll();
    
    @Query("""
    	    SELECT p FROM Payment p 
    	    WHERE (:search IS NULL OR LOWER(p.fromAccount) LIKE LOWER(CONCAT('%', :search, '%')) 
    	           OR LOWER(p.toAccount) LIKE LOWER(CONCAT('%', :search, '%')))
    	    AND (:refType IS NULL OR p.refType = :refType)
    	    AND (:paymentStatus IS NULL OR p.paymentStatus = :paymentStatus)
    	    """)
    	Page<Payment> filterPayments(@Param("search") String search,
    	                             @Param("refType") Payment.RefType refType,
    	                             @Param("paymentStatus") Payment.PaymentStatus paymentStatus,
    	                             Pageable pageable);



}