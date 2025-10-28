package com.business.salesync.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🧾 Expense Entity - Master record for all business expenses
 * Tracks office expenses, utilities, salaries, and other operational costs
 */
@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique Expense Reference
    @Column(name = "expense_ref_no", unique = true, nullable = false, length = 50)
    private String expenseRefNo; // EXP-20251026-0001

    // Date Information
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "due_date")
    private LocalDate dueDate; // For credit purchases

    // Category & Classification
    @Column(name = "expense_category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ExpenseCategory expenseCategory;

    @Column(name = "expense_type", length = 50)
    @Enumerated(EnumType.STRING)
    private ExpenseType expenseType; // OPERATIONAL, CAPITAL, etc.

    // Vendor/Supplier Information
    @Column(name = "vendor_id")
    private Long vendorId; // Links to Supplier table

    @Column(name = "vendor_name", length = 150)
    private String vendorName;

    @Column(name = "vendor_contact", length = 50)
    private String vendorContact;

    // Description & Reference
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "reference_no", length = 100)
    private String referenceNo; // External invoice/bill number

    @Column(name = "attachment_url", length = 255)
    private String attachmentUrl; // Receipt/invoice scan

    // Financial Details (using BigDecimal for precision)
    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "discount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Payment Tracking
    @Column(name = "amount_paid", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "amount_due", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amountDue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // Cash, Bank Transfer, Credit Card, bKash

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // Financial Account Tracking
    @Column(name = "fin_account_id")
    private Long finAccountId; // Links to FinancialAccount

    @Column(name = "fin_account_name", length = 100)
    private String finAccountName; // Cash at Hand, Pubali Bank, etc.

    // Approval Workflow
    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approval_remarks", length = 255)
    private String approvalRemarks;

    // Project/Department Allocation
    @Column(name = "department", length = 100)
    private String department; // Sales, Admin, IT, etc.

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_name", length = 150)
    private String projectName;

    @Column(name = "cost_center", length = 100)
    private String costCenter;

    // Recurring Expense
    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "recurring_frequency", length = 20)
    private String recurringFrequency; // MONTHLY, QUARTERLY, YEARLY

    @Column(name = "next_occurrence_date")
    private LocalDate nextOccurrenceDate;

    // Remarks & Notes
    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "internal_notes", length = 500)
    private String internalNotes; // Private notes not visible to vendor

    // Financial Year
    @Column(name = "financial_year", length = 20)
    private String financialYear; // 2024-2025

    // Soft Delete
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // One-to-Many relationship with ExpenseItem
//    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private List<ExpenseItem> expenseItems = new ArrayList<>();
    
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private List<ExpenseItem> expenseItems = new ArrayList<>();

    // Enums
    public enum ExpenseCategory {
        RENT,                  // Office/Warehouse rent
        UTILITIES,             // Electricity, Water, Gas
        SALARIES,              // Employee salaries
        OFFICE_SUPPLIES,       // Stationery, supplies
        MAINTENANCE,           // Repairs, maintenance
        TRANSPORTATION,        // Vehicle, fuel, transport
        MARKETING,             // Advertising, promotions
        INSURANCE,             // Business insurance
        TAXES,                 // Business taxes
        COMMUNICATION,         // Phone, Internet
        PROFESSIONAL_FEES,     // Legal, Accounting
        EQUIPMENT,             // Office equipment
        SOFTWARE,              // Software subscriptions
        TRAVEL,                // Business travel
        MEALS,                 // Business meals
        TRAINING,              // Employee training
        MISCELLANEOUS          // Other expenses
    }

    public enum ExpenseType {
        OPERATIONAL,           // Day-to-day operations
        CAPITAL,               // Long-term investments
        ADMINISTRATIVE,        // Admin costs
        SALES,                 // Sales-related
        PRODUCTION             // Manufacturing costs
    }

    public enum PaymentStatus {
        PENDING,
        PARTIALLY_PAID,
        PAID,
        OVERDUE
    }

    // Helper Methods

    /**
     * Calculate total amount from items
     */
    public void calculateTotals() {
        this.subtotal = expenseItems.stream()
                .map(item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate tax
        if (taxPercentage != null && taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = subtotal.multiply(taxPercentage).divide(new BigDecimal("100"));
        }

        // Calculate total
        this.totalAmount = subtotal
                .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                .subtract(discount != null ? discount : BigDecimal.ZERO);

        // Calculate due amount
        this.amountDue = totalAmount.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);
    }

    /**
     * Update payment status based on amount paid
     */
    public void updatePaymentStatus() {
        if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) == 0) {
            this.paymentStatus = PaymentStatus.PENDING;
        } else if (amountPaid.compareTo(totalAmount) < 0) {
            this.paymentStatus = PaymentStatus.PARTIALLY_PAID;
        } else {
            this.paymentStatus = PaymentStatus.PAID;
        }

        // Check if overdue
        if (dueDate != null && LocalDate.now().isAfter(dueDate) && 
            this.paymentStatus != PaymentStatus.PAID) {
            this.paymentStatus = PaymentStatus.OVERDUE;
        }
    }

    /**
     * Add expense item
     */
    public void addExpenseItem(ExpenseItem item) {
        expenseItems.add(item);
        item.setExpense(this);
    }

    /**
     * Remove expense item
     */
    public void removeExpenseItem(ExpenseItem item) {
        expenseItems.remove(item);
        item.setExpense(null);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.expenseDate == null) {
            this.expenseDate = LocalDate.now();
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
        
        // Initialize BigDecimal fields
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        if (taxPercentage == null) taxPercentage = BigDecimal.ZERO;
        if (discount == null) discount = BigDecimal.ZERO;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (amountPaid == null) amountPaid = BigDecimal.ZERO;
        if (amountDue == null) amountDue = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
