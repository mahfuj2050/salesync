package com.business.salesync.models;

import java.time.LocalDate;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // Cash, Bank, Credit Card etc.

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    @Column(name = "vendor_name", length = 100)
    private String vendorName;

    @Column(name = "payment_status", length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // NEW FIELD
    
    public enum PaymentStatus {
        PENDING,
        PARTIALLY_PAID,
        PAID
    }

    // Helper method to determine status based on amount
    public void updatePaymentStatus(Double amountPaid) {
        if (amountPaid == null || amountPaid <= 0) {
            this.paymentStatus = PaymentStatus.PENDING;
        } else if (amountPaid < this.amount) {
            this.paymentStatus = PaymentStatus.PARTIALLY_PAID;
        } else {
            this.paymentStatus = PaymentStatus.PAID;
        }
    }
}

