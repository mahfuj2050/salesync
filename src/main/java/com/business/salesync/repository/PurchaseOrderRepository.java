package com.business.salesync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.business.salesync.models.PurchaseOrder;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Find all orders for a specific supplier
    List<PurchaseOrder> findBySupplierId(Long supplierId);

    // Find orders by status
    List<PurchaseOrder> findByStatus(String status);

    // Search by purchase order number (partial match)
    List<PurchaseOrder> findByPurchaseOrderNoContainingIgnoreCase(String purchaseOrderNo);

    // Find latest PurchaseOrder for generating next PO number
    PurchaseOrder findTopByOrderByInsertDateDesc();

    // FIXED: total amount purchased from a supplier
    @Query("SELECT COALESCE(SUM(po.totalAmount), 0) FROM PurchaseOrder po WHERE po.supplier.id = :supplierId")
    Double getTotalPurchasedAmountBySupplier(@Param("supplierId") Long supplierId);
    
    // New method: find today's purchase orders
    @Query("SELECT po FROM PurchaseOrder po WHERE po.insertDate = CURRENT_DATE")
    List<PurchaseOrder> findTodayPurchaseOrders();
}
