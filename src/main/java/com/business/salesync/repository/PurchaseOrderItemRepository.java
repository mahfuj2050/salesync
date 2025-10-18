package com.business.salesync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.PurchaseOrderItem;

import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    // Find all items for a specific purchase order
    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);

    // Find items by product
    List<PurchaseOrderItem> findByProductId(Long productId);

    // Optional: sum quantity purchased for a product
    @Query("SELECT SUM(poi.quantity) FROM PurchaseOrderItem poi WHERE poi.product.id = :productId")
    Integer getTotalQuantityPurchased(Long productId);

    // Optional: sum total cost for a product
    @Query("SELECT SUM(poi.quantity * poi.purchasePrice) FROM PurchaseOrderItem poi WHERE poi.product.id = :productId")
    Double getTotalCostForProduct(Long productId);
}
