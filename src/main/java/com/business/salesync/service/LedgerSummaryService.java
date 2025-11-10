package com.business.salesync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.business.salesync.dto.LedgerDTO;
import com.business.salesync.dto.LedgerSummaryDTO;
import com.business.salesync.models.FinancialAccount;
import com.business.salesync.repository.FinancialAccountRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 📊 Ledger Service - Dynamic Customer & Supplier Ledger Generation
 * 
 * This service generates ledger reports dynamically from FinancialAccount table
 * No separate ledger tables needed - all data comes from one source
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LedgerSummaryService {

    private final FinancialAccountRepository financialAccountRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    // ========================================
    // 📊 CUSTOMER LEDGER METHODS
    // ========================================

    /**
     * 🔹 Get Customer Ledger - All Transactions
     * 
     * @param customerName Customer name (supports partial match, null = all customers)
     * @return Complete ledger summary with all transactions
     */
    public LedgerSummaryDTO getCustomerLedger(String customerName) {
        log.info("📊 Generating customer ledger for: {}", 
                customerName != null ? customerName : "ALL CUSTOMERS");
        
        List<FinancialAccount> accounts = 
                financialAccountRepository.getCustomerLedger(customerName);
        
        if (accounts.isEmpty()) {
            String msg = customerName != null 
                    ? "No transactions found for customer: " + customerName
                    : "No customer transactions found in the system";
            log.warn("⚠️ {}", msg);
            return LedgerSummaryDTO.empty(msg);
        }
        
        log.info("✅ Found {} customer transactions", accounts.size());
        return buildLedgerSummary(accounts, customerName, "CUSTOMER", null, null);
    }

    /**
     * 🔹 Get Customer Ledger - With Date Range
     * 
     * @param customerName Customer name (optional)
     * @param fromDate Start date (optional)
     * @param toDate End date (optional)
     * @return Ledger summary filtered by date range
     */
    public LedgerSummaryDTO getCustomerLedgerByDateRange(
            String customerName, 
            LocalDateTime fromDate, 
            LocalDateTime toDate) {
        
        log.info("📅 Generating customer ledger - Name: {}, From: {}, To: {}", 
                customerName, fromDate, toDate);
        
        List<FinancialAccount> accounts = financialAccountRepository
                .getCustomerLedgerByDateRange(customerName, fromDate, toDate);
        
        if (accounts.isEmpty()) {
            String msg = "No transactions found for the specified date range";
            log.warn("⚠️ {}", msg);
            return LedgerSummaryDTO.empty(msg);
        }
        
        log.info("✅ Found {} customer transactions in date range", accounts.size());
        return buildLedgerSummary(accounts, customerName, "CUSTOMER", fromDate, toDate);
    }

    // ========================================
    // 🏭 SUPPLIER LEDGER METHODS
    // ========================================

    /**
     * 🔹 Get Supplier Ledger - All Transactions
     * 
     * @param supplierName Supplier name (supports partial match, null = all suppliers)
     * @return Complete ledger summary with all transactions
     */
    public LedgerSummaryDTO getSupplierLedger(String supplierName) {
        log.info("🏭 Generating supplier ledger for: {}", 
                supplierName != null ? supplierName : "ALL SUPPLIERS");
        
        List<FinancialAccount> accounts = 
                financialAccountRepository.getSupplierLedger(supplierName);
        
        if (accounts.isEmpty()) {
            String msg = supplierName != null 
                    ? "No transactions found for supplier: " + supplierName
                    : "No supplier transactions found in the system";
            log.warn("⚠️ {}", msg);
            return LedgerSummaryDTO.empty(msg);
        }
        
        log.info("✅ Found {} supplier transactions", accounts.size());
        return buildLedgerSummary(accounts, supplierName, "SUPPLIER", null, null);
    }

    /**
     * 🔹 Get Supplier Ledger - With Date Range
     * 
     * @param supplierName Supplier name (optional)
     * @param fromDate Start date (optional)
     * @param toDate End date (optional)
     * @return Ledger summary filtered by date range
     */
    public LedgerSummaryDTO getSupplierLedgerByDateRange(
            String supplierName, 
            LocalDateTime fromDate, 
            LocalDateTime toDate) {
        
        log.info("📅 Generating supplier ledger - Name: {}, From: {}, To: {}", 
                supplierName, fromDate, toDate);
        
        List<FinancialAccount> accounts = financialAccountRepository
                .getSupplierLedgerByDateRange(supplierName, fromDate, toDate);
        
        if (accounts.isEmpty()) {
            String msg = "No transactions found for the specified date range";
            log.warn("⚠️ {}", msg);
            return LedgerSummaryDTO.empty(msg);
        }
        
        log.info("✅ Found {} supplier transactions in date range", accounts.size());
        return buildLedgerSummary(accounts, supplierName, "SUPPLIER", fromDate, toDate);
    }

    // ========================================
    // 📈 SUMMARY METHODS
    // ========================================

    /**
     * 🔹 Get Customer Total Receivable
     * Shows how much money customer owes to the business
     * 
     * @param customerName Customer name (optional)
     * @return Total receivable amount
     */
    public Double getCustomerTotalReceivable(String customerName) {
        log.info("💰 Calculating customer receivable for: {}", customerName);
        
        Double receivable = financialAccountRepository
                .getCustomerTotalReceivable(customerName);
        
        double amount = receivable != null ? receivable : 0.0;
        log.info("✅ Customer receivable: ৳ {}", amount);
        
        return amount;
    }

    /**
     * 🔹 Get Supplier Total Payable
     * Shows how much money business owes to supplier
     * 
     * @param supplierName Supplier name (optional)
     * @return Total payable amount
     */
    public Double getSupplierTotalPayable(String supplierName) {
        log.info("💸 Calculating supplier payable for: {}", supplierName);
        
        Double payable = financialAccountRepository
                .getSupplierTotalPayable(supplierName);
        
        double amount = payable != null ? payable : 0.0;
        log.info("✅ Supplier payable: ৳ {}", amount);
        
        return amount;
    }

    /**
     * 🔹 Get All Customer Names
     * Returns list of unique customer names (for dropdowns)
     * 
     * @return List of customer names
     */
    public List<String> getAllCustomerNames() {
        log.info("📋 Fetching all customer names");
        
        List<String> names = financialAccountRepository.getAllCustomerNames();
        
        log.info("✅ Found {} unique customers", names.size());
        return names;
    }

    /**
     * 🔹 Get All Supplier Names
     * Returns list of unique supplier names (for dropdowns)
     * 
     * @return List of supplier names
     */
    public List<String> getAllSupplierNames() {
        log.info("📋 Fetching all supplier names");
        
        List<String> names = financialAccountRepository.getAllSupplierNames();
        
        log.info("✅ Found {} unique suppliers", names.size());
        return names;
    }

    // ========================================
    // 🛠️ PRIVATE HELPER METHODS
    // ========================================

    /**
     * 🔹 Build Complete Ledger Summary with Financial Calculations
     * 
     * This method:
     * 1. Converts entities to DTOs
     * 2. Calculates total debit/credit
     * 3. Calculates opening/closing balance
     * 4. Calculates net balance (receivable/payable)
     */
    private LedgerSummaryDTO buildLedgerSummary(
            List<FinancialAccount> accounts,
            String entityName,
            String entityType,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        
        log.debug("🔧 Building ledger summary for {} transactions", accounts.size());
        
        // Convert entities to DTOs
        List<LedgerDTO> transactions = accounts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // Calculate total debit (money going out)
        double totalDebit = accounts.stream()
                .mapToDouble(a -> a.getDebitAmount() != null ? a.getDebitAmount() : 0.0)
                .sum();
        
        // Calculate total credit (money coming in)
        double totalCredit = accounts.stream()
                .mapToDouble(a -> a.getCreditAmount() != null ? a.getCreditAmount() : 0.0)
                .sum();
        
        // Calculate opening balance (balance before first transaction)
        double openingBalance = 0.0;
        if (!accounts.isEmpty()) {
            FinancialAccount first = accounts.get(0);
            double firstBalance = first.getBalanceAfterTransaction() != null 
                    ? first.getBalanceAfterTransaction() : 0.0;
            double firstDebit = first.getDebitAmount() != null 
                    ? first.getDebitAmount() : 0.0;
            double firstCredit = first.getCreditAmount() != null 
                    ? first.getCreditAmount() : 0.0;
            
            openingBalance = firstBalance - firstDebit + firstCredit;
        }
        
        // Calculate closing balance (balance after last transaction)
        double closingBalance = 0.0;
        if (!accounts.isEmpty()) {
            FinancialAccount last = accounts.get(accounts.size() - 1);
            closingBalance = last.getBalanceAfterTransaction() != null 
                    ? last.getBalanceAfterTransaction() : 0.0;
        }
        
        // Calculate net balance
        // For customers: credit - debit (positive = customer owes us)
        // For suppliers: debit - credit (positive = we owe supplier)
        double netBalance;
        if ("CUSTOMER".equalsIgnoreCase(entityType)) {
            netBalance = totalCredit - totalDebit; // Receivable
        } else {
            netBalance = totalDebit - totalCredit; // Payable
        }
        
        log.debug("📊 Summary - Debit: ৳{}, Credit: ৳{}, Net: ৳{}", 
                totalDebit, totalCredit, netBalance);
        
        // Build and return summary
        return LedgerSummaryDTO.builder()
                .entityName(entityName != null ? entityName : "All " + entityType + "s")
                .entityType(entityType)
                .fromDate(fromDate != null ? fromDate.format(DATE_FORMATTER) : "All Time")
                .toDate(toDate != null ? toDate.format(DATE_FORMATTER) : "All Time")
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .netBalance(netBalance)
                .totalTransactions(transactions.size())
                .transactions(transactions)
                .message("Ledger generated successfully")
                .success(true)
                .build();
    }

    /**
     * 🔹 Convert FinancialAccount Entity to LedgerDTO
     */
    private LedgerDTO convertToDTO(FinancialAccount account) {
        return LedgerDTO.builder()
                .id(account.getId())
                .trnRefNo(account.getTrnRefNo())
                .trnDate(account.getTrnDate())
                .transactionType(account.getTransactionType())
                .refType(account.getRefType())
                .paymentMethod(account.getPaymentMethod())
                .debitAmount(account.getDebitAmount())
                .creditAmount(account.getCreditAmount())
                .balanceAfterTrn(account.getBalanceAfterTransaction())
                .entityName(account.getEntityName())
                .entityType(account.getEntityType())
                .finAccName(account.getFinAccName())
                .finAccType(account.getFinAccType())
                .paymentStatus(account.getPaymentStatus())
                .remarks(account.getRemarks())
                .createdBy(account.getCreatedBy())
                .createdAt(account.getCreatedAt())
                .build();
    }
    
    
    
    /**
     * 🔹 Get Transaction Details by ID
     * 
     * @param transactionId Transaction ID
     * @return Map with transaction details and related transactions
     */
    public Map<String, Object> getTransactionDetails(Long transactionId) {
        log.info("📄 Fetching transaction details for ID: {}", transactionId);
        
        // Find the transaction
        FinancialAccount transaction = financialAccountRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
        
        // Convert to DTO
        LedgerDTO transactionDTO = convertToDTO(transaction);
        
        // Find related transactions (same entity, same ref_id or similar trn_ref_no)
        List<FinancialAccount> relatedAccounts = financialAccountRepository
                .findRelatedTransactions(
                        transaction.getEntityName(),
                        transaction.getEntityType(),
                        transaction.getRefId()
                );
        
        List<LedgerDTO> relatedTransactions = relatedAccounts.stream()
                .filter(acc -> !acc.getId().equals(transactionId)) // Exclude current transaction
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("transaction", transactionDTO);
        response.put("relatedTransactions", relatedTransactions);
        
        log.info("✅ Transaction details retrieved - Related transactions: {}", relatedTransactions.size());
        
        return response;
    }
}
