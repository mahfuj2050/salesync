package com.business.salesync.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.ExpenseItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 🧾 Expense Item Repository
 * Handles all database operations for ExpenseItem entity
 */
@Repository
public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, Long> {

    // ========================================
    // 🔍 BASIC QUERIES
    // ========================================

    /**
     * Find all items for a specific expense
     */
    List<ExpenseItem> findByExpenseId(Long expenseId);
    
   // List<ExpenseItem> findByProductId(Long productId);


    /**
     * Find all items for a specific expense ordered by id
     */
    List<ExpenseItem> findByExpenseIdOrderById(Long expenseId);

    /**
     * Count items for an expense
     */
    long countByExpenseId(Long expenseId);

    /**
     * Delete all items for an expense
     */
    void deleteByExpenseId(Long expenseId);

    // ========================================
    // 🔍 SEARCH QUERIES
    // ========================================

    /**
     * Find items by name
     */
    @Query("SELECT ei FROM ExpenseItem ei WHERE " +
           "LOWER(ei.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))")
    List<ExpenseItem> findByItemNameContainingIgnoreCase(@Param("itemName") String itemName);

    /**
     * Find items by category
     */
    List<ExpenseItem> findByCategory(String category);

    /**
     * Find items by serial number
     */
    List<ExpenseItem> findBySerialNumber(String serialNumber);

    /**
     * Search items by keyword
     */
    @Query("SELECT ei FROM ExpenseItem ei WHERE " +
           "LOWER(ei.itemName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ei.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ei.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ExpenseItem> searchItems(@Param("keyword") String keyword);

    // ========================================
    // 💰 FINANCIAL QUERIES
    // ========================================

    /**
     * Calculate total amount for an expense
     */
    @Query("SELECT COALESCE(SUM(ei.total), 0) FROM ExpenseItem ei " +
           "WHERE ei.expense.id = :expenseId")
    BigDecimal getTotalAmountByExpenseId(@Param("expenseId") Long expenseId);

    /**
     * Calculate total quantity for an expense
     */
    @Query("SELECT COALESCE(SUM(ei.quantity), 0) FROM ExpenseItem ei " +
           "WHERE ei.expense.id = :expenseId")
    BigDecimal getTotalQuantityByExpenseId(@Param("expenseId") Long expenseId);

    /**
     * Calculate total tax for an expense
     */
    @Query("SELECT COALESCE(SUM(ei.taxAmount), 0) FROM ExpenseItem ei " +
           "WHERE ei.expense.id = :expenseId")
    BigDecimal getTotalTaxByExpenseId(@Param("expenseId") Long expenseId);

    /**
     * Calculate total discount for an expense
     */
    @Query("SELECT COALESCE(SUM(ei.discount), 0) FROM ExpenseItem ei " +
           "WHERE ei.expense.id = :expenseId")
    BigDecimal getTotalDiscountByExpenseId(@Param("expenseId") Long expenseId);

    // ========================================
    // 📊 ASSET QUERIES
    // ========================================

    /**
     * Find items marked as assets
     */
    @Query("SELECT ei FROM ExpenseItem ei WHERE ei.isAsset = true")
    List<ExpenseItem> findAssetItems();

    /**
     * Find asset items for an expense
     */
    @Query("SELECT ei FROM ExpenseItem ei WHERE ei.expense.id = :expenseId " +
           "AND ei.isAsset = true")
    List<ExpenseItem> findAssetItemsByExpenseId(@Param("expenseId") Long expenseId);

    /**
     * Find items by asset ID
     */
    List<ExpenseItem> findByAssetId(Long assetId);

    /**
     * Count asset items
     */
    @Query("SELECT COUNT(ei) FROM ExpenseItem ei WHERE ei.isAsset = true")
    long countAssetItems();

    // ========================================
    // 📈 ANALYTICS QUERIES
    // ========================================

    /**
     * Get most purchased items
     */
    @Query("SELECT ei.itemName, COUNT(ei), COALESCE(SUM(ei.total), 0) " +
           "FROM ExpenseItem ei " +
           "GROUP BY ei.itemName " +
           "ORDER BY COUNT(ei) DESC")
    List<Object[]> getMostPurchasedItems();

    /**
     * Get items by category summary
     */
    @Query("SELECT ei.category, COUNT(ei), COALESCE(SUM(ei.total), 0) " +
           "FROM ExpenseItem ei " +
           "WHERE ei.category IS NOT NULL " +
           "GROUP BY ei.category " +
           "ORDER BY SUM(ei.total) DESC")
    List<Object[]> getItemSummaryByCategory();

    /**
     * Get items with highest value
     */
    @Query("SELECT ei FROM ExpenseItem ei " +
           "ORDER BY ei.total DESC")
    List<ExpenseItem> getHighestValueItems();

    /**
     * Get average item price
     */
    @Query("SELECT AVG(ei.unitPrice) FROM ExpenseItem ei")
    BigDecimal getAverageItemPrice();

    /**
     * Get total items count
     */
    @Query("SELECT COUNT(ei) FROM ExpenseItem ei")
    long getTotalItemsCount();

    // ========================================
    // 🔧 UTILITY QUERIES
    // ========================================

    /**
     * Find items with warranty expiring soon
     */
    @Query("SELECT ei FROM ExpenseItem ei WHERE " +
           "ei.warrantyExpiry IS NOT NULL AND " +
           "ei.warrantyExpiry BETWEEN CURRENT_DATE AND :futureDate " +
           "ORDER BY ei.warrantyExpiry ASC")
    List<ExpenseItem> findItemsWithExpiringWarranty(@Param("futureDate") java.time.LocalDate futureDate);

    /**
     * Find items with expired warranty
     */
    @Query("SELECT ei FROM ExpenseItem ei WHERE " +
           "ei.warrantyExpiry IS NOT NULL AND " +
           "ei.warrantyExpiry < CURRENT_DATE")
    List<ExpenseItem> findItemsWithExpiredWarranty();

    /**
     * Find items by unit
     */
    List<ExpenseItem> findByUnit(String unit);

    /**
     * Find items by price range
     */
    @Query("SELECT ei FROM ExpenseItem ei WHERE " +
           "ei.unitPrice BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY ei.unitPrice ASC")
    List<ExpenseItem> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    /**
     * Find items for multiple expenses
     */
    @Query("SELECT ei FROM ExpenseItem ei WHERE ei.expense.id IN :expenseIds " +
           "ORDER BY ei.expense.id, ei.id")
    List<ExpenseItem> findByExpenseIds(@Param("expenseIds") List<Long> expenseIds);
}