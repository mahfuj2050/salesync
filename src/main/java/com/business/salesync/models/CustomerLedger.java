package com.business.salesync.models;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "trn_ref_no", length = 50)
    private String trnRefNo;

    @Column(name = "trn_type", length = 50)
    private String trnType; // SALE, PAYMENT, RETURN

    @Column(name = "debit_amount")
    private Double debitAmount;

    @Column(name = "credit_amount")
    private Double creditAmount;

    @Column(name = "balance_after")
    private Double balanceAfter;

    @Column(name = "remarks", length = 200)
    private String remarks;

    @Column(name = "trn_date")
    private LocalDateTime trnDate;

    @PrePersist
    public void onCreate() {
        if (trnDate == null) trnDate = LocalDateTime.now();
    }
}
