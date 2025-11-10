package com.business.salesync.repository;

import com.business.salesync.models.FinancialAccount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, Long> {
   FinancialAccount findByTrnRefNo(String trnRefNo);

   List<FinancialAccount> findByCustomerId(Long customerId);

   List<FinancialAccount> findBySupplierId(Long supplierId);

   List<FinancialAccount> findByFinAccId(String finAccId);

   List<FinancialAccount> findByFinAccType(String finAccType);

   List<FinancialAccount> findByFinAccNameIgnoreCase(String finAccName);

   @Query("SELECT DISTINCT f FROM FinancialAccount f WHERE f.finAccType = :finAccType")
   List<FinancialAccount> findByFinAccTypeDistinct(@Param("finAccType") String finAccType);

   Optional<FinancialAccount> findTopByOrderByIdDesc();

   List<FinancialAccount> findByFinAccName(String finAccName);

   @Query("SELECT COALESCE(SUM(f.currentBalance), 0) FROM FinancialAccount f")
   Double getTotalBalance();

   @Query("SELECT COALESCE(SUM(f.debitAmount), 0) FROM FinancialAccount f WHERE f.transactionType = 'CASH_OUT'")
   Double getTotalDebit();

   @Query("SELECT COALESCE(SUM(f.creditAmount), 0) FROM FinancialAccount f WHERE f.transactionType = 'CASH_IN'")
   Double getTotalCredit();

   @Query("SELECT f FROM FinancialAccount f WHERE f.entityType = 'CUSTOMER' AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%'))) ORDER BY f.trnDate ASC, f.id ASC")
   List<FinancialAccount> getCustomerLedger(@Param("entityName") String entityName);

   @Query("SELECT f FROM FinancialAccount f WHERE f.entityType = 'CUSTOMER' AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%'))) AND (:fromDate IS NULL OR f.trnDate >= :fromDate) AND (:toDate IS NULL OR f.trnDate <= :toDate) ORDER BY f.trnDate ASC, f.id ASC")
   List<FinancialAccount> getCustomerLedgerByDateRange(@Param("entityName") String entityName, @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

   @Query("SELECT f FROM FinancialAccount f WHERE f.entityType = 'SUPPLIER' AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%'))) ORDER BY f.trnDate ASC, f.id ASC")
   List<FinancialAccount> getSupplierLedger(@Param("entityName") String entityName);

   @Query("SELECT f FROM FinancialAccount f WHERE f.entityType = 'SUPPLIER' AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%'))) AND (:fromDate IS NULL OR f.trnDate >= :fromDate) AND (:toDate IS NULL OR f.trnDate <= :toDate) ORDER BY f.trnDate ASC, f.id ASC")
   List<FinancialAccount> getSupplierLedgerByDateRange(@Param("entityName") String entityName, @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

   @Query("SELECT COALESCE(SUM(f.debitAmount) - SUM(f.creditAmount), 0) FROM FinancialAccount f WHERE f.entityType = 'CUSTOMER' AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%')))")
   Double getCustomerTotalReceivable(@Param("entityName") String entityName);

   @Query("SELECT COALESCE(SUM(f.creditAmount) - SUM(f.debitAmount), 0) FROM FinancialAccount f WHERE f.entityType = 'SUPPLIER' AND (:entityName IS NULL OR LOWER(f.entityName) LIKE LOWER(CONCAT('%', :entityName, '%')))")
   Double getSupplierTotalPayable(@Param("entityName") String entityName);

   @Query("SELECT DISTINCT f.entityName FROM FinancialAccount f WHERE f.entityType = 'CUSTOMER' AND f.entityName IS NOT NULL ORDER BY f.entityName ASC")
   List<String> getAllCustomerNames();

   @Query("SELECT DISTINCT f.entityName FROM FinancialAccount f WHERE f.entityType = 'SUPPLIER' AND f.entityName IS NOT NULL ORDER BY f.entityName ASC")
   List<String> getAllSupplierNames();

   @Query("SELECT f FROM FinancialAccount f WHERE f.entityName = :entityName AND f.entityType = :entityType AND (:refId IS NULL OR f.refId = :refId) ORDER BY f.trnDate DESC")
   List<FinancialAccount> findRelatedTransactions(@Param("entityName") String entityName, @Param("entityType") String entityType, @Param("refId") Long refId);

   @Query(
      value = "SELECT fa.*\nFROM app_acct_financial_accounts fa\nINNER JOIN (\n    SELECT fin_acc_name, MAX(id) as max_id\n    FROM app_acct_financial_accounts\n    GROUP BY fin_acc_name\n) latest ON fa.id = latest.max_id\nORDER BY fa.fin_acc_name ASC\n",
      nativeQuery = true
   )
   List<FinancialAccount> findLatestBalancePerAccount();
}