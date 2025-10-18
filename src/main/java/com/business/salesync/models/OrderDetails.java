package com.business.salesync.models;

import java.math.BigDecimal;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.*;


@Entity
@Table(name = "order_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private SalesOrder order;
    
    @NotNull
    @Column(name = "invoice_number")
    private String invoiceNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @NotNull
    @Column(nullable = false)
    private int quantity;

    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "vat", precision = 10, scale = 2)
    private BigDecimal vat = BigDecimal.ZERO;

    @Column(name = "total_price", precision = 10, scale  = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @PrePersist
    @PreUpdate
    protected void calculateTotals() {
        if (unitPrice != null && quantity > 0) {
            // Calculate total price before VAT
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            
            // Calculate VAT if not set
            if (vat == null || vat.compareTo(BigDecimal.ZERO) == 0) {
                // Assuming 15% VAT - adjust as needed
                vat = lineTotal.multiply(new BigDecimal("0.15"));
            }
            
            // Calculate total price including VAT
            totalPrice = lineTotal.add(vat);
        }
    }
}