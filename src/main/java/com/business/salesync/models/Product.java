package com.business.salesync.models;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity 
@Table(name = "products",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "sku"),
           @UniqueConstraint(columnNames = "barcode")
       })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 📦 Basic Identifiers
    @Column(name = "sku", nullable = false, length = 50, unique = true)
    private String sku;

    @Column(name = "barcode", length = 50, unique = true)
    private String barcode;

    @NotBlank
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 🏷️ Category, Brand, Supplier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    @JsonIgnore
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @JsonIgnore
    private Supplier supplier;

    // 💲 Pricing & VAT
    @NotNull
    @DecimalMin("0.00")
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @DecimalMin("0.00")
    @Column(name = "vat_percent", precision = 5, scale = 2)
    private BigDecimal vatPercent;

    // 📊 Stock Information
    @NotNull
    @Range(min = 0)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @Range(min = 0)
    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel = 2;

    // 🛡️ Warranty & Guarantee
    @Column(name = "warranty_period")
    private Integer warrantyPeriod; // Warranty period in months

    @Column(name = "warranty_description", length = 255)
    private String warrantyDescription; // Warranty terms/details

    @Column(name = "guarantee_period")
    private Integer guaranteePeriod; // Guarantee period in months

    @Column(name = "guarantee_description", length = 255)
    private String guaranteeDescription; // Guarantee terms/details

    // 📦 Additional Attributes
    @Column(name = "unit_of_measure", length = 50)
    private String unitOfMeasure;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "size", length = 50)
    private String size;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "flavor", length = 50)
    private String flavor;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // 📈 Profitability Tracking
    @Transient
    public BigDecimal getProfitPerUnit() {
        if (sellingPrice == null || costPrice == null) return BigDecimal.ZERO;
        return sellingPrice.subtract(costPrice);
    }

    // 🛡️ Helper methods for warranty/guarantee
    @Transient
    public boolean hasWarranty() {
        return warrantyPeriod != null && warrantyPeriod > 0;
    }

    @Transient
    public boolean hasGuarantee() {
        return guaranteePeriod != null && guaranteePeriod > 0;
    }

    // 📅 Auditing
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // Ensure quantity is not null
        if (this.quantity == null) this.quantity = 0;
        if (this.minStockLevel == null) this.minStockLevel = 0;
        
        // Ensure BigDecimal fields are not null
        if (this.costPrice == null) this.costPrice = BigDecimal.ZERO;
        if (this.sellingPrice == null) this.sellingPrice = BigDecimal.ZERO;
        if (this.vatPercent == null) this.vatPercent = BigDecimal.ZERO;
        
        // Set default values for warranty/guarantee if needed
        if (this.warrantyPeriod == null) this.warrantyPeriod = 0;
        if (this.guaranteePeriod == null) this.guaranteePeriod = 0;
        
        // Set empty values for trigger to generate SKU, barcode, and batch number
        if (this.sku == null || this.sku.trim().isEmpty()) {
            this.sku = "";
        }
        if (this.barcode == null || this.barcode.trim().isEmpty()) {
            this.barcode = "";
        }
        if (this.batchNo == null || this.batchNo.trim().isEmpty()) {
            this.batchNo = "";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        
        // Ensure critical fields are not null on update as well
        if (this.quantity == null) this.quantity = 0;
        if (this.minStockLevel == null) this.minStockLevel = 0;
        if (this.costPrice == null) this.costPrice = BigDecimal.ZERO;
        if (this.sellingPrice == null) this.sellingPrice = BigDecimal.ZERO;
        if (this.warrantyPeriod == null) this.warrantyPeriod = 0;
        if (this.guaranteePeriod == null) this.guaranteePeriod = 0;
    }
}