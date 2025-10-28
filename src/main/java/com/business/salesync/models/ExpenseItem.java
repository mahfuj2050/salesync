package com.business.salesync.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * 🧾 Expense Item Entity - Individual line items for each expense
 * Allows detailed breakdown of expenses with multiple items
 */
@Entity
@Table(name = "expense_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to parent Expense
	/*
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "expense_id", nullable = false) private Expense expense;
	 */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @JsonBackReference
    private Expense expense;
    
    // Item Details
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", length = 100)
    private String category; // Sub-category

    // Quantity & Unit
    @Column(name = "quantity", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit", length = 50)
    private String unit; // pcs, kg, liters, hours, etc.

    // Pricing
    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    // Tax & Discount (item level)
    @Column(name = "tax_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    // Additional Info
    @Column(name = "serial_number", length = 100)
    private String serialNumber; // For equipment/assets

    @Column(name = "warranty_expiry")
    private java.time.LocalDate warrantyExpiry;

    @Column(name = "remarks", length = 255)
    private String remarks;

    // Asset Tracking (if item is a fixed asset)
    @Column(name = "is_asset")
    @Builder.Default
    private Boolean isAsset = false;

    @Column(name = "asset_id")
    private Long assetId; // Links to Asset table if exists

    // Helper Methods

    /**
     * Calculate total amount for this item
     */
    public void calculateAmount() {
        // Base amount = quantity * unit price
        this.amount = (quantity != null ? quantity : BigDecimal.ONE)
                .multiply(unitPrice != null ? unitPrice : BigDecimal.ZERO);

        // Calculate tax
        if (taxPercentage != null && taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = amount.multiply(taxPercentage).divide(new BigDecimal("100"));
        } else {
            this.taxAmount = BigDecimal.ZERO;
        }

        // Calculate total
        this.total = amount
                .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                .subtract(discount != null ? discount : BigDecimal.ZERO);
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        calculateAmount();
    }
}