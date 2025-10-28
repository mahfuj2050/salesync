package com.business.salesync.models;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_ref_no", unique = true, nullable = false)
    private String returnRefNo; // e.g. PRN-20251001-1001

    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "total_return_amount")
    private Double totalReturnAmount;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @OneToMany(mappedBy = "purchaseReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseReturnItem> items;

    @PrePersist
    public void onCreate() {
        if (returnDate == null) returnDate = LocalDateTime.now();
    }
}
