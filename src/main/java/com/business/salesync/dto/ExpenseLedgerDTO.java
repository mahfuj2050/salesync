package com.business.salesync.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseLedgerDTO {
    
    private Long id;
    private String ledgerCode;
    private String ledgerName;
    private String ledgerCategory;
    private String ledgerType;
    private String description;
    private Long parentLedgerId;
    private String parentLedgerName;
    
    // Budget
    private BigDecimal monthlyBudget;
    private BigDecimal quarterlyBudget;
    private BigDecimal yearlyBudget;
    
    // Approval
    private Boolean requiresApproval;
    private BigDecimal approvalLimit;
    
    // Alerts
    private BigDecimal budgetAlertThreshold;
    private Boolean enableAlerts;
    
    // Status
    private Boolean isActive;
    private Integer displayOrder;
    
    // UI
    private String icon;
    private String colorCode;
    
    // Financial Year
    private String financialYear;
    
    // Audit
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    // Summary (for reporting)
    private BigDecimal totalExpenses;
    private BigDecimal totalPaid;
    private BigDecimal totalDue;
    private Long expenseCount;
    private BigDecimal budgetUtilization;
}