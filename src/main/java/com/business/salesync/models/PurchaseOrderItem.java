package com.business.salesync.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_order_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "purchase_price", nullable = false)
    private Double purchasePrice;
    
    @Column(name = "selling_price", nullable = false)
    private Double sellingPrice;

    @Column(name = "subtotal")
    private Double subtotal;
    
    @Column(name = "vat_percent")
    private Double vatPercent;
    
    @Column(name = "vat_amount") 
    private Double vatAmount;
}