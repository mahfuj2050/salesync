package com.business.salesync.models;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Where;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense_ledgers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class ExpenseLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ledger_code", unique = true, nullable = false, length = 50)
    private String ledgerCode; // LEDGER-001

    @Column(name = "ledger_name", unique = true, nullable = false, length = 100)
    private String ledgerName; // Office Rent, Electricity Bill, Staff Salaries

    @Column(name = "ledger_category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LedgerCategory ledgerCategory; // RENT, UTILITIES, SALARIES, etc.

    @Column(name = "ledger_type", length = 50)
    @Enumerated(EnumType.STRING)
    private LedgerType ledgerType; // OPERATIONAL, CAPITAL, ADMINISTRATIVE

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "parent_ledger_id")
    private Long parentLedgerId; // For sub-ledgers

    // Budget Allocation
    @Column(name = "monthly_budget", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal monthlyBudget = BigDecimal.ZERO;

    @Column(name = "quarterly_budget", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal quarterlyBudget = BigDecimal.ZERO;

    @Column(name = "yearly_budget", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal yearlyBudget = BigDecimal.ZERO;

    // Approval Settings
    @Column(name = "requires_approval")
    @Builder.Default
    private Boolean requiresApproval = false;

    @Column(name = "approval_limit", precision = 12, scale = 2)
    private BigDecimal approvalLimit; // Expenses above this need approval

    // Alert Settings
    @Column(name = "budget_alert_threshold", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal budgetAlertThreshold = new BigDecimal("80.00"); // Alert at 80% usage

    @Column(name = "enable_alerts")
    @Builder.Default
    private Boolean enableAlerts = true;

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder;

    // UI Customization
    @Column(name = "icon", length = 50)
    private String icon; // FontAwesome icon name

    @Column(name = "color_code", length = 20)
    private String colorCode; // #FF5733

    // Financial Year
    @Column(name = "financial_year", length = 20)
    private String financialYear; // 2024-2025

    // Soft Delete
    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    // Audit Fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Enums
    public enum LedgerCategory {
        RENT,
        UTILITIES,
        SALARIES,
        OFFICE_SUPPLIES,
        MAINTENANCE,
        TRANSPORTATION,
        MARKETING,
        INSURANCE,
        TAXES,
        COMMUNICATION,
        PROFESSIONAL_FEES,
        EQUIPMENT,
        SOFTWARE,
        TRAVEL,
        MEALS,
        TRAINING,
        MISCELLANEOUS
    }

    public enum LedgerType {
        OPERATIONAL,
        CAPITAL,
        ADMINISTRATIVE,
        SALES,
        PRODUCTION
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.ledgerCode == null) {
            this.ledgerCode = "LEDGER-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}