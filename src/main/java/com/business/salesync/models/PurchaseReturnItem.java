package com.business.salesync.models;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purchase_return_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_return_id")
    private PurchaseReturn purchaseReturn;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity_returned")
    private Integer quantityReturned;

    @Column(name = "unit_cost")
    private Double unitCost;

    @Column(name = "total_cost")
    private Double totalCost;

    @Column(name = "remarks", length = 200)
    private String remarks;
}
