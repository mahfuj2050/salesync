package com.business.salesync.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "from_account", length = 100)
    private String fromAccount;

    @Column(name = "to_account", length = 100)
    private String toAccount;

    @Column(name = "instrument_no", length = 100)
    private String instrumentNo;

    @Column(name = "method", length = 50)
    private String method;

    // Financial fields - CHANGED from double to BigDecimal
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "total_vat", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalVat;

    @Column(name = "discount", precision = 12, scale = 2, nullable = false)
    private BigDecimal discount;

    @Column(name = "grand_total", precision = 12, scale = 2, nullable = false)
    private BigDecimal grandTotal;

    @Column(name = "amount_paid", precision = 12, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "amount_due", precision = 12, scale = 2, nullable = false)
    private BigDecimal amountDue;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "ref_id", nullable = false)
    private Long refId; // links to Order.id or PurchaseOrder.id

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", length = 30, nullable = false)
    private RefType refType;
    
    @Column(name = "entity_type", length = 50)
    private String entityType; // e.g., "Customer" or "Supplier"
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "payment_type", length = 50)
    private String paymentType; // e.g., "Revenue" or "Expense"


    @Column(name = "remarks", length = 255)
    private String remarks;
    
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;
    
    // CHANGED from double to BigDecimal
    @Column(name = "paid_amount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;

    public enum PaymentStatus {
        PENDING,
        PARTIALLY_PAID,
        PAID
    }

    // UPDATED method to work with BigDecimal
    public void updatePaymentStatus(BigDecimal amountPaid) {
        if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            this.paymentStatus = PaymentStatus.PENDING;
        } else if (amountPaid.compareTo(this.grandTotal) < 0) {
            this.paymentStatus = PaymentStatus.PARTIALLY_PAID;
        } else {
            this.paymentStatus = PaymentStatus.PAID;
        }
    }

    public enum RefType {
        SALE_ORDER,
        PURCHASE_ORDER,
        EXPENSE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paymentDate == null) {
            paymentDate = createdAt;
        }
        
        // Initialize BigDecimal fields if they are null
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (totalVat == null) totalVat = BigDecimal.ZERO;
        if (discount == null) discount = BigDecimal.ZERO;
        if (grandTotal == null) grandTotal = BigDecimal.ZERO;
        if (amountPaid == null) amountPaid = BigDecimal.ZERO;
        if (amountDue == null) amountDue = BigDecimal.ZERO;
        if (paidAmount == null) paidAmount = BigDecimal.ZERO;
    }
}
