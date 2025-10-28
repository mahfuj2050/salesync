package com.business.salesync.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.FinancialAccount;
import com.business.salesync.models.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, Long> {
	
	 // Find payments linked to a specific customer
	List<FinancialAccount> findByCustomerId(Long customerId);
	
	   // Find payments linked to a specific supplier
   List<FinancialAccount> findBySupplierId(Long supplierId);

    // Optional: find by account ID
	List<FinancialAccount> findByFinAccId(String finAccId);

    // Optional: find by account type (CASH, BANK, MOBILE)
    List<FinancialAccount> findByFinAccType(String finAccType);

    Optional<FinancialAccount> findTopByOrderByIdDesc();

    List<FinancialAccount> findByFinAccName(String finAccName);

    /** 🔹 Total Current Balance (sum of all accounts) */
    @Query("SELECT COALESCE(SUM(f.currentBalance), 0) FROM FinancialAccount f")
    Double getTotalBalance();

    /** 🔹 Total Debit = All CASH_OUT (money going out) */
    @Query("SELECT COALESCE(SUM(f.debitAmount), 0) FROM FinancialAccount f WHERE f.transactionType = 'CASH_OUT'")
    Double getTotalDebit();

    /** 🔹 Total Credit = All CASH_IN (money coming in) */
    @Query("SELECT COALESCE(SUM(f.creditAmount), 0) FROM FinancialAccount f WHERE f.transactionType = 'CASH_IN'")
    Double getTotalCredit();

    // ========================================
    // 📊 CUSTOMER LEDGER QUERIES
    // ========================================

    /**
     * 🔹 Get Customer Ledger - All Transactions (No Date Filter)
     * Returns all transactions for a specific customer ordered by transaction date
     */
    @Query("SELECT f FROM FinancialAccount f " +
           "WHERE f.entityType = 'CUSTOMER' " +
           "AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%'))) " +
           "ORDER BY f.trnDate ASC, f.id ASC")
    List<FinancialAccount> getCustomerLedger(@Param("entityName") String entityName);

    /**
     * 🔹 Get Customer Ledger - With Date Range Filter
     * Returns customer transactions between fromDate and toDate
     * Handles null dates gracefully
     */
    @Query("SELECT f FROM FinancialAccount f " +
           "WHERE f.entityType = 'CUSTOMER' " +
           "AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%'))) " +
           "AND (:fromDate IS NULL OR f.trnDate >= :fromDate) " +
           "AND (:toDate IS NULL OR f.trnDate <= :toDate) " +
           "ORDER BY f.trnDate ASC, f.id ASC")
    List<FinancialAccount> getCustomerLedgerByDateRange(
            @Param("entityName") String entityName,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    // ========================================
    // 🏭 SUPPLIER LEDGER QUERIES
    // ========================================

    /**
     * 🔹 Get Supplier Ledger - All Transactions (No Date Filter)
     * Returns all transactions for a specific supplier ordered by transaction date
     */
    @Query("SELECT f FROM FinancialAccount f " +
           "WHERE f.entityType = 'SUPPLIER' " +
           "AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%'))) " +
           "ORDER BY f.trnDate ASC, f.id ASC")
    List<FinancialAccount> getSupplierLedger(@Param("entityName") String entityName);

    /**
     * 🔹 Get Supplier Ledger - With Date Range Filter
     * Returns supplier transactions between fromDate and toDate
     * Handles null dates gracefully
     */
    @Query("SELECT f FROM FinancialAccount f " +
           "WHERE f.entityType = 'SUPPLIER' " +
           "AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%'))) " +
           "AND (:fromDate IS NULL OR f.trnDate >= :fromDate) " +
           "AND (:toDate IS NULL OR f.trnDate <= :toDate) " +
           "ORDER BY f.trnDate ASC, f.id ASC")
    List<FinancialAccount> getSupplierLedgerByDateRange(
            @Param("entityName") String entityName,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    // ========================================
    // 📈 LEDGER SUMMARY QUERIES
    // ========================================

    /**
     * 🔹 Get Customer Total Receivable (Debit - Credit for CUSTOMER)
     * Shows how much money customers owe
     */
    @Query("SELECT COALESCE(SUM(f.debitAmount) - SUM(f.creditAmount), 0) " +
           "FROM FinancialAccount f " +
           "WHERE f.entityType = 'CUSTOMER' " +
           "AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%')))")
    Double getCustomerTotalReceivable(@Param("entityName") String entityName);

    /**
     * 🔹 Get Supplier Total Payable (Credit - Debit for SUPPLIER)
     * Shows how much money business owes to suppliers
     */
    @Query("SELECT COALESCE(SUM(f.creditAmount) - SUM(f.debitAmount), 0) " +
           "FROM FinancialAccount f " +
           "WHERE f.entityType = 'SUPPLIER' " +
           "AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%')))")
    Double getSupplierTotalPayable(@Param("entityName") String entityName);

    /**
     * 🔹 Get All Unique Customer Names
     * Useful for dropdown/autocomplete in frontend
     */
    @Query("SELECT DISTINCT f.entityName FROM FinancialAccount f " +
           "WHERE f.entityType = 'CUSTOMER' " +
           "AND f.entityName IS NOT NULL " +
           "ORDER BY f.entityName ASC")
    List<String> getAllCustomerNames();

    /**
     * 🔹 Get All Unique Supplier Names
     * Useful for dropdown/autocomplete in frontend
     */
    @Query("SELECT DISTINCT f.entityName FROM FinancialAccount f " +
           "WHERE f.entityType = 'SUPPLIER' " +
           "AND f.entityName IS NOT NULL " +
           "ORDER BY f.entityName ASC")
    List<String> getAllSupplierNames();
    
    
    /**
     * 🔹 Find Related Transactions
     * Returns transactions related to the same entity and reference
     */
    @Query("SELECT f FROM FinancialAccount f " +
           "WHERE f.entityName = :entityName " +
           "AND f.entityType = :entityType " +
           "AND (:refId IS NULL OR f.refId = :refId) " +
           "ORDER BY f.trnDate DESC")
    List<FinancialAccount> findRelatedTransactions(
            @Param("entityName") String entityName,
            @Param("entityType") String entityType,
            @Param("refId") Long refId
    );
    
    @Query("SELECT fa FROM FinancialAccount fa WHERE fa.id IN " +
    	       "(SELECT MAX(fa2.id) FROM FinancialAccount fa2 GROUP BY fa2.finAccType, fa2.finAccName)")
    	List<FinancialAccount> findLatestBalancePerAccount();


}
