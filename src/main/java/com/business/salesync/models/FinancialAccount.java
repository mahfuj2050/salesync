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
    private String finAccId;

    @Column(name = "fin_acc_name", nullable = false, length = 100)
    private String finAccName;

    @Column(name = "current_balance", nullable = false)
    private Double currentBalance;

    @Column(name = "opening_balance")
    private Double openingBalance;

    @Column(name = "amount_paid")
    private Double amountPaid;

    @Column(name = "amount_received")
    private Double amountReceived;

    @Column(name = "trn_date")
    private LocalDateTime trnDate;

    @Column(name = "trn_ref_no")
    private String trnRefNo;

    // SALE / PURCHASE / EXPENSE / ADJUSTMENT
    @Column(name = "transaction_type", length = 20)
    private String transactionType;

    @Column(name = "financial_year")
    private String financialYear;

    @Column(name = "payment_status")
    private String paymentStatus;

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

