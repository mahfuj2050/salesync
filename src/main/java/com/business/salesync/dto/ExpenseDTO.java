package com.business.salesync.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 🧾 Expense DTO for form submission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDTO {
    
    private Long id;
    private String expenseRefNo;
    private LocalDate expenseDate;
    private LocalDate dueDate;
    private String expenseCategory;
    private String expenseType;
    
    // Vendor
    private Long vendorId;
    private String vendorName;
    private String vendorContact;
    
    // Description
    private String description;
    private String referenceNo;
    private String attachmentUrl;
    
    // Financial
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal taxPercentage;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal amountDue;
    
    // Payment
    private String paymentStatus;
    private String paymentMethod;
    private String paymentDate;
    
    // Account
    private Long finAccountId;
    private String finAccountName;
    
    // Approval
    private Boolean isApproved;
    private String approvedBy;
    private String approvalRemarks;
    
    // Project
    private String department;
    private Long projectId;
    private String projectName;
    private String costCenter;
    
    // Recurring
    private Boolean isRecurring;
    private String recurringFrequency;
    private LocalDate nextOccurrenceDate;
    
    // Notes
    private String remarks;
    private String internalNotes;
    
    // Financial Year
    private String financialYear;
    
    // Items
    @Builder.Default
    private List<ExpenseItemDTO> expenseItems = new ArrayList<>();
    
    // Audit
    private String createdBy;
}