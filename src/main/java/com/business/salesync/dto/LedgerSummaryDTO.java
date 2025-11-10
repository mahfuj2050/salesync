package com.business.salesync.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 📈 Ledger Summary DTO - Complete ledger report with transactions and summary
 * This wraps multiple LedgerDTO transactions along with financial summaries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerSummaryDTO {

    // Entity Information
    private String entityName;              // Customer/Supplier Name
    private String entityType;              // CUSTOMER / SUPPLIER
    
    // Date Range
    private String fromDate;                // Start date of report
    private String toDate;                  // End date of report
    
    // Financial Summary
    private Double totalDebit;              // Total money out (sum of all debits)
    private Double totalCredit;             // Total money in (sum of all credits)
    private Double openingBalance;          // Starting balance
    private Double closingBalance;          // Ending balance
    private Double netBalance;              // Net amount owed/due
    
    // Transaction Count
    private Integer totalTransactions;      // Number of transactions
    
    // Transaction Details (List of LedgerDTO)
    private List<LedgerDTO> transactions;
    
    // Status Message
    private String message;                 // Success/error message
    private Boolean success;                // Operation success flag
    
    // Helper Methods
    
    /**
     * Get balance status message
     * For customers: Shows receivable amount
     * For suppliers: Shows payable amount
     */
    public String getBalanceStatus() {
        if (netBalance == null) return "N/A";
        
        if ("CUSTOMER".equalsIgnoreCase(entityType)) {
            return netBalance > 0 ? "Receivable: ৳ " + String.format("%.2f", netBalance) : "No Due";
        } else if ("SUPPLIER".equalsIgnoreCase(entityType)) {
            return netBalance > 0 ? "Payable: ৳ " + String.format("%.2f", netBalance) : "No Due";
        }
        return "Balance: ৳ " + String.format("%.2f", netBalance);
    }
    
    /**
     * Get formatted opening balance
     */
    public String getFormattedOpeningBalance() {
        return openingBalance != null 
            ? String.format("৳ %.2f", openingBalance) 
            : "৳ 0.00";
    }
    
    /**
     * Get formatted closing balance
     */
    public String getFormattedClosingBalance() {
        return closingBalance != null 
            ? String.format("৳ %.2f", closingBalance) 
            : "৳ 0.00";
    }
    
    /**
     * Get formatted total debit
     */
    public String getFormattedTotalDebit() {
        return totalDebit != null 
            ? String.format("৳ %.2f", totalDebit) 
            : "৳ 0.00";
    }
    
    /**
     * Get formatted total credit
     */
    public String getFormattedTotalCredit() {
        return totalCredit != null 
            ? String.format("৳ %.2f", totalCredit) 
            : "৳ 0.00";
    }
    
    /**
     * Create an empty ledger summary (for error cases or no data)
     */
    public static LedgerSummaryDTO empty(String message) {
        return LedgerSummaryDTO.builder()
                .totalDebit(0.0)
                .totalCredit(0.0)
                .openingBalance(0.0)
                .closingBalance(0.0)
                .netBalance(0.0)
                .totalTransactions(0)
                .transactions(List.of())
                .message(message)
                .success(true)
                .build();
    }
}