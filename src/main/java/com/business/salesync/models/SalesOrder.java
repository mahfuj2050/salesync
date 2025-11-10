package com.business.salesync.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE orders SET deleted = true WHERE id = ?")
//@Where(clause = "deleted = false")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    // Financial fields
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "total_vat", precision = 10, scale = 2)
    private BigDecimal totalVat = BigDecimal.ZERO;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "grand_total", precision = 10, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "amount_paid", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "amount_due", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountDue = BigDecimal.ZERO;

    @NotNull
    @Column(name = "date_ordered", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOrdered;
    
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_account_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private FinancialAccount financialAccount;

    // Timestamps
    private LocalDateTime insertDate;
    
    private LocalDateTime updateDate;

    // Relationships
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderDetails> orderDetails = new ArrayList<>();

    // Status fields
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "payment_status", length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String status; // ORDER_STATUS, COMPLETED, CANCELLED, etc.
    
    private String remarks;
    
    // 🛡️ Warranty & Guarantee
    @Column(name = "warranty_period")
    private Integer warrantyPeriod; // Warranty period in months

    @Column(name = "warranty_description", length = 255)
    private String warrantyDescription; // Warranty terms/details

    @Column(name = "guarantee_period")
    private Integer guaranteePeriod; // Guarantee period in months

    @Column(name = "guarantee_description", length = 255)
    private String guaranteeDescription; // Guarantee terms/details
    

    public enum PaymentStatus {
        PENDING,
        PARTIALLY_PAID,
        PAID
    }

    @PrePersist
    protected void onCreate() {
        insertDate = LocalDateTime.now();
        updateDate = insertDate;
        if (dateOrdered == null) {
            dateOrdered = LocalDate.now();
        }
        
        // Set default values for warranty/guarantee if needed
        if (this.warrantyPeriod == null) this.warrantyPeriod = 0;
        if (this.guaranteePeriod == null) this.guaranteePeriod = 0;
        
        // Auto-calculate amounts if not set
        if (grandTotal == null || grandTotal.compareTo(BigDecimal.ZERO) == 0) {
            grandTotal = totalAmount.subtract(discount != null ? discount : BigDecimal.ZERO);
        }
        if (amountDue == null || amountDue.compareTo(BigDecimal.ZERO) == 0) {
            amountDue = grandTotal.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);
        }
        updatePaymentStatus();
    }

    @PreUpdate
    protected void onUpdate() {
    	
        if (this.warrantyPeriod == null) this.warrantyPeriod = 0;
        if (this.guaranteePeriod == null) this.guaranteePeriod = 0;
        
        updateDate = LocalDateTime.now();
        updatePaymentStatus();
    }

    public void updatePaymentStatus() {
        if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            this.paymentStatus = PaymentStatus.PENDING;
        } else if (amountPaid.compareTo(grandTotal) < 0) {
            this.paymentStatus = PaymentStatus.PARTIALLY_PAID;
        } else {
            this.paymentStatus = PaymentStatus.PAID;
        }
        // Update amountDue
        this.amountDue = grandTotal.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);
    }

    public void addOrderDetail(OrderDetails detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
    }

    public void removeOrderDetail(OrderDetails detail) {
        orderDetails.remove(detail);
        detail.setOrder(null);
    }
    
    @Transient
    public String getWarrantyLabel() {
        if (warrantyPeriod == null || warrantyPeriod <= 0) {
            return "No Warranty";
        }

        String label = warrantyPeriod + " month" + (warrantyPeriod > 1 ? "s" : "");
        if (getWarrantyExpiryDate() != null) {
            label += " (Expires: " + getWarrantyExpiryDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ")";
        }
        return label;
    }
    
    @Transient
    public LocalDate getWarrantyExpiryDate() {
        if (dateOrdered != null && warrantyPeriod != null && warrantyPeriod > 0) {
            return dateOrdered.plusMonths(warrantyPeriod);
        }
        return null;
    }

	/*
	 * @Transient public boolean isWarrantyExpired() { LocalDate expiry =
	 * getWarrantyExpiryDate(); return expiry != null &&
	 * expiry.isBefore(LocalDate.now()); }
	 */
    
    @Transient
    public String getGuaranteeLabel() {
        if (guaranteePeriod == null || guaranteePeriod <= 0) {
            return "No Guarantee";
        }

        String label = guaranteePeriod + " month" + (guaranteePeriod > 1 ? "s" : "");
        if (getGuaranteeExpiryDate() != null) {
            label += " (Expires: " + getGuaranteeExpiryDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ")";
        }
        return label;
    }

    @Transient
    public LocalDate getGuaranteeExpiryDate() {
        if (dateOrdered != null && guaranteePeriod != null && guaranteePeriod > 0) {
            return dateOrdered.plusMonths(guaranteePeriod);
        }
        return null;
    }

	/*
	 * @Transient public boolean isGuaranteeExpired() { LocalDate expiry =
	 * getGuaranteeExpiryDate(); return expiry != null &&
	 * expiry.isBefore(LocalDate.now()); }
	 */
    

    public boolean isWarrantyExpired() {
        // your expiration logic, e.g.
        if (warrantyPeriod == null || dateOrdered == null) return false;
        return dateOrdered.plusMonths(warrantyPeriod).isBefore(LocalDate.now());
    }

    public boolean isGuaranteeExpired() {
        if (guaranteePeriod == null || dateOrdered == null) return false;
        return dateOrdered.plusMonths(guaranteePeriod).isBefore(LocalDate.now());
    }

}