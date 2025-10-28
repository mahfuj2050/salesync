package com.business.salesync.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 📊 Ledger DTO - Formatted response for Customer/Supplier Ledger
 * This DTO represents a single transaction in the ledger
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerDTO {

    private Long id;
    
    // Transaction Information
    private String trnRefNo;              // Invoice/PO Number (e.g., INV-20250929-8828)
    private LocalDateTime trnDate;         // Transaction Date
    private String transactionType;        // CASH_IN / CASH_OUT
    private String refType;                // SALE_ORDER / PURCHASE_ORDER / PAYMENT
    private String paymentMethod;          // Cash / bKash / Bank Transfer
    
    // Financial Fields
    private Double debitAmount;            // Money going out (expenses, purchases)
    private Double creditAmount;           // Money coming in (sales, payments received)
    private Double balanceAfterTrn;        // Running balance after this transaction
    
    // Entity Information
    private String entityName;             // Customer/Supplier Name
    private String entityType;             // CUSTOMER / SUPPLIER
    
    // Account Information
    private String finAccName;             // Cash at Hand, Pubali Bank, bKash, etc.
    private String finAccType;             // CASH / BANK / MFS
    
    // Status & Metadata
    private String paymentStatus;          // PAID / PENDING / PARTIALLY_PAID
    private String remarks;                // Additional notes
    private String createdBy;              // User who created the entry
    private LocalDateTime createdAt;       // When this record was created
    
    // Helper Methods
    
    /**
     * Get formatted transaction date
     */
    public String getFormattedDate() {
        if (trnDate == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        return trnDate.format(formatter);
    }
    
    /**
     * Get formatted debit amount with currency
     */
    public String getFormattedDebit() {
        return debitAmount != null && debitAmount > 0 
            ? String.format("৳ %.2f", debitAmount) 
            : "-";
    }
    
    /**
     * Get formatted credit amount with currency
     */
    public String getFormattedCredit() {
        return creditAmount != null && creditAmount > 0 
            ? String.format("৳ %.2f", creditAmount) 
            : "-";
    }
    
    /**
     * Get formatted balance with currency
     */
    public String getFormattedBalance() {
        return balanceAfterTrn != null 
            ? String.format("৳ %.2f", balanceAfterTrn) 
            : "৳ 0.00";
    }
    
    /**
     * Check if this is a payment transaction
     */
    public boolean isPayment() {
        return "PAYMENT".equalsIgnoreCase(refType);
    }
    
    /**
     * Check if this is a sale/purchase transaction
     */
    public boolean isSaleOrPurchase() {
        return "SALE_ORDER".equalsIgnoreCase(refType) || 
               "PURCHASE_ORDER".equalsIgnoreCase(refType);
    }
}