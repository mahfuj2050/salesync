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

    // Timestamps
    private LocalDateTime insertDate;
    private LocalDateTime updateDate;

    // Relationships
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
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
}