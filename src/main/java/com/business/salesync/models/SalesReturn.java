package com.business.salesync.models;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales_returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_ref_no", unique = true, nullable = false)
    private String returnRefNo; // e.g. SRN-20251001-1001

    @ManyToOne
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "total_return_amount")
    private Double totalReturnAmount;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @OneToMany(mappedBy = "salesReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesReturnItem> items;

    @PrePersist
    public void onCreate() {
        if (returnDate == null) returnDate = LocalDateTime.now();
    }
}
