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
import jakarta.persistence.PostLoad;
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
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "total_vat", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalVat = BigDecimal.ZERO;

    @Column(name = "discount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "grand_total", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "amount_paid", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "amount_due", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amountDue = BigDecimal.ZERO;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "ref_id", nullable = false)
    private Long refId; // links to Order.id or PurchaseOrder.id
    
    @Column(name = "trn_ref_no")
    private String trnRefNo; // Invoice number, Expense Ref, Payment Ref

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", length = 30, nullable = false)
    @Builder.Default
    private RefType refType = RefType.SALE_ORDER; // Default value
    
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
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    public void setRefType(RefType refType) {
        this.refType = refType;
    }
    
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
        
        // Initialize BigDecimal fields if they are null (double safety with @Builder.Default)
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (totalVat == null) totalVat = BigDecimal.ZERO;
        if (discount == null) discount = BigDecimal.ZERO;
        if (grandTotal == null) grandTotal = BigDecimal.ZERO;
        if (amountPaid == null) amountPaid = BigDecimal.ZERO;
        if (amountDue == null) amountDue = BigDecimal.ZERO;
        if (paidAmount == null) paidAmount = BigDecimal.ZERO;
        
        // ✅ Ensure enums are never null
        if (refType == null) {
            refType = RefType.SALE_ORDER;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
        
        // Auto-calculate amount due if not set
        if (amountDue.compareTo(BigDecimal.ZERO) == 0 && grandTotal.compareTo(BigDecimal.ZERO) > 0) {
            amountDue = grandTotal.subtract(amountPaid);
        }
        
        // Auto-update payment status
        updatePaymentStatus(amountPaid);
    }

    // ✅ Add this method to handle invalid enum values during database read
    @PostLoad
    private void validateEnums() {
        // If refType is null (due to invalid database value), set a default
        if (this.refType == null) {
            System.err.println("⚠️ Invalid ref_type value found in database for payment ID: " + this.id);
            this.refType = RefType.SALE_ORDER;
        }
        
        // Similarly for paymentStatus
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
        
        // Ensure financial fields are never null
        if (this.totalAmount == null) this.totalAmount = BigDecimal.ZERO;
        if (this.totalVat == null) this.totalVat = BigDecimal.ZERO;
        if (this.discount == null) this.discount = BigDecimal.ZERO;
        if (this.grandTotal == null) this.grandTotal = BigDecimal.ZERO;
        if (this.amountPaid == null) this.amountPaid = BigDecimal.ZERO;
        if (this.amountDue == null) this.amountDue = BigDecimal.ZERO;
        if (this.paidAmount == null) this.paidAmount = BigDecimal.ZERO;
    }

    // ✅ Add custom setter for refType to handle string conversion if needed
    public void setRefType(String refTypeString) {
        if (refTypeString == null || refTypeString.trim().isEmpty()) {
            this.refType = RefType.SALE_ORDER;
            return;
        }
        
        try {
            this.refType = RefType.valueOf(refTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Invalid RefType provided: '" + refTypeString + "'. Using SALE_ORDER as default.");
            this.refType = RefType.SALE_ORDER;
        }
    }

    // ✅ Add custom setter for paymentStatus to handle string conversion if needed
    public void setPaymentStatus(String paymentStatusString) {
        if (paymentStatusString == null || paymentStatusString.trim().isEmpty()) {
            this.paymentStatus = PaymentStatus.PENDING;
            return;
        }
        
        try {
            this.paymentStatus = PaymentStatus.valueOf(paymentStatusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Invalid PaymentStatus provided: '" + paymentStatusString + "'. Using PENDING as default.");
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }
}