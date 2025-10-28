package com.business.salesync.models;


import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_acct_financial_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fin_acc_id", unique = true, nullable = false)
    private String finAccId; // FIN-1001-90001

    @Column(name = "fin_acc_name", nullable = false, length = 100)
    private String finAccName; // Cash, Pubali Bank, bKash

    @Column(name = "fin_acc_type", length = 50)
    private String finAccType; // CASH, BANK, MOBILE, etc.

    @Column(name = "opening_balance")
    private Double openingBalance;

    @Column(name = "debit_amount")
    private Double debitAmount;

    @Column(name = "credit_amount")
    private Double creditAmount;

    @Column(name = "balance_after_trn")
    private Double balanceAfterTransaction;

    @Column(name = "current_balance", nullable = false)
    private Double currentBalance;

    @Column(name = "trn_date")
    private LocalDateTime trnDate;

    @Column(name = "trn_ref_no")
    private String trnRefNo; // Invoice number, Expense Ref, Payment Ref

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "ref_type", length = 50)
    private String refType; // SALE_ORDER, PURCHASE_ORDER, EXPENSE, PAYMENT

    @Column(name = "transaction_type", length = 50)
    private String transactionType; // CASH_IN / CASH_OUT

    @Column(name = "entity_name", length = 150)
    private String entityName; // Customer or Supplier name

    @Column(name = "entity_type", length = 50)
    private String entityType; // CUSTOMER / SUPPLIER / EXPENSE / INTERNAL
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // Cash / bKash / Bank Transfer

    @Column(name = "financial_year")
    private String financialYear;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "is_posted")
    private Boolean isPosted = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.trnDate == null) this.trnDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Create financial entry for expense
     */
    public static FinancialAccount createExpenseEntry(
            Expense expense,
            String finAccountName,
            String finAccountType,
            Double currentBalance,
            String createdBy) {
        
        return FinancialAccount.builder()
                .finAccId("FIN-EXP-" + expense.getId())
                .finAccName(finAccountName)
                .finAccType(finAccountType)
                .debitAmount(0.0)
                .creditAmount(expense.getTotalAmount().doubleValue())
                .balanceAfterTransaction(currentBalance - expense.getTotalAmount().doubleValue())
                .currentBalance(currentBalance - expense.getTotalAmount().doubleValue())
                .trnDate(LocalDateTime.now())
                .trnRefNo(expense.getExpenseRefNo())
                .refId(expense.getId())
                .refType("EXPENSE")
                .transactionType("CASH_OUT")
                .entityName(expense.getVendorName() != null ? expense.getVendorName() : "Internal Expense")
                .entityType("EXPENSE")
                .paymentMethod(expense.getPaymentMethod())
                .paymentStatus(expense.getPaymentStatus().toString())
                .remarks(expense.getDescription())
                .isPosted(true)
                .createdBy(createdBy)
                .build();
    }
}
