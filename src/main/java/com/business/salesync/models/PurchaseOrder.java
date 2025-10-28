package com.business.salesync.models;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "purchase_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "purchase_order_no")
    private String purchaseOrderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "insert_date")
    private LocalDate insertDate;
    
    @Column(name = "update_date")
    private LocalDate updateDate;

    // Financial fields with correct column names
    @Column(name = "total_amount")
    private double totalAmount;
    
    @Column(name = "total_vat")
    private double totalVat;
    
    private double discount;
    
    @Column(name = "grand_total")
    private double grandTotal;
    
    @Column(name = "amount_paid")
    private double amountPaid;
    
    @Column(name = "amount_due")
    private double amountDue;
    
    @Column(name = "vat_amount", precision = 12, scale = 2)
    private BigDecimal vatAmount;

    private String status;
    private String remarks;
    
    @Column(name = "payment_status", length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    public enum PaymentStatus {
        PENDING,
        PARTIALLY_PAID,
        PAID
    }

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PurchaseOrderItem> items = new ArrayList<>(); // Initialize the list
    
    @Transient
    private String itemsJson;

    public String getItemsJson() {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson) {
        this.itemsJson = itemsJson;
    }
    
    // Helper method to safely add items
    public void addItem(PurchaseOrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setPurchaseOrder(this);
    }
    
    // Helper method to safely set items
    public void setItems(List<PurchaseOrderItem> newItems) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        } else {
            this.items.clear(); // Clear existing items safely
        }
        
        if (newItems != null) {
            for (PurchaseOrderItem item : newItems) {
                addItem(item); // Use the safe add method
            }
        }
    }

    
    @PrePersist
    protected void onCreate() {
        if (insertDate == null) {
            insertDate = LocalDate.now();
        }
        if (updateDate == null) {
            updateDate = insertDate;
        }
        if (paymentStatus == null) {
            updatePaymentStatus(amountPaid);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDate.now();
        updatePaymentStatus(amountPaid);
    }

    public void updatePaymentStatus(Double amountPaid) {
        if (amountPaid == null || amountPaid <= 0) {
            this.paymentStatus = PaymentStatus.PENDING;
        } else if (amountPaid < this.grandTotal) {
            this.paymentStatus = PaymentStatus.PARTIALLY_PAID;
        } else {
            this.paymentStatus = PaymentStatus.PAID;
        }
    }
}
