package com.business.salesync.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sales_return_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_return_id")
    private SalesReturn salesReturn;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity_returned")
    private Integer quantityReturned;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "reason", length = 200)
    private String reason;
}
