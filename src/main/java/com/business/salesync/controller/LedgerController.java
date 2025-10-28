package com.business.salesync.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.business.salesync.dto.LedgerSummaryDTO;
import com.business.salesync.service.LedgerSummaryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LedgerController {

	    private final LedgerSummaryService ledgerService;

	    // ========================================
	    // 📊 CUSTOMER LEDGER ENDPOINTS
	    // ========================================

	    @GetMapping("/customer")
	    public ResponseEntity<LedgerSummaryDTO> getCustomerLedger(
	            @RequestParam(required = false) String name) {
	        
	        log.info("📊 REST API: Fetching customer ledger for: {}", 
	                name != null ? name : "ALL CUSTOMERS");
	        
	        try {
	            LedgerSummaryDTO ledger = ledgerService.getCustomerLedger(name);
	            
	            log.info("✅ Customer ledger retrieved successfully. Transactions: {}", 
	                    ledger.getTotalTransactions());
	            
	            return ResponseEntity.ok(ledger);
	            
	        } catch (Exception e) {
	            log.error("❌ Error fetching customer ledger: {}", e.getMessage(), e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(LedgerSummaryDTO.empty("Error: " + e.getMessage()));
	        }
	    }

	    /**
	     * 🔹 GET Customer Ledger - With Date Range Filter
	     * 
	     * Endpoint: GET /api/ledger/customer/date-range
	     * Query Params:
	     *   - name (optional): Customer name
	     *   - fromDate (optional): Start date (ISO format: 2025-10-01T00:00:00)
	     *   - toDate (optional): End date (ISO format: 2025-10-31T23:59:59)
	     * 
	     * Example: GET /api/ledger/customer/date-range?name=Rahim&fromDate=2025-10-01T00:00:00&toDate=2025-10-31T23:59:59
	     * 
	     * @param name Customer name (optional)
	     * @param fromDate Start date (optional)
	     * @param toDate End date (optional)
	     * @return Filtered ledger within date range
	     */
	    @GetMapping("/customer/date-range")
	    public ResponseEntity<LedgerSummaryDTO> getCustomerLedgerByDateRange(
	            @RequestParam(required = false) String name,
	            @RequestParam(required = false) 
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
	            @RequestParam(required = false) 
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
	        
	        log.info("📅 REST API: Fetching customer ledger - Name: {}, From: {}, To: {}", 
	                name != null ? name : "ALL", fromDate, toDate);
	        
	        try {
	            LedgerSummaryDTO ledger = ledgerService.getCustomerLedgerByDateRange(
	                    name, fromDate, toDate);
	            
	            log.info("✅ Customer ledger (date range) retrieved. Transactions: {}", 
	                    ledger.getTotalTransactions());
	            
	            return ResponseEntity.ok(ledger);
	            
	        } catch (Exception e) {
	            log.error("❌ Error fetching customer ledger by date: {}", e.getMessage(), e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(LedgerSummaryDTO.empty("Error: " + e.getMessage()));
	        }
	    }

	    /**
	     * 🔹 GET Customer Total Receivable
	     * 
	     * Endpoint: GET /api/ledger/customer/receivable
	     * Query Params:
	     *   - name (optional): Customer name
	     * 
	     * Returns how much money the customer owes to the business
	     * 
	     * Example: GET /api/ledger/customer/receivable?name=Rahim Traders
	     * 
	     * @param name Customer name (optional)
	     * @return Total receivable amount with status
	     */
	    @GetMapping("/customer/receivable")
	    public ResponseEntity<Map<String, Object>> getCustomerReceivable(
	            @RequestParam(required = false) String name) {
	        
	        log.info("💰 REST API: Calculating customer receivable for: {}", 
	                name != null ? name : "ALL CUSTOMERS");
	        
	        try {
	            Double receivable = ledgerService.getCustomerTotalReceivable(name);
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("customerName", name != null ? name : "All Customers");
	            response.put("totalReceivable", receivable);
	            response.put("formattedAmount", String.format("৳ %.2f", receivable));
	            response.put("status", receivable > 0 ? "DUE" : "CLEAR");
	            response.put("message", receivable > 0 
	                    ? "Customer has outstanding balance" 
	                    : "No outstanding balance");
	            response.put("success", true);
	            
	            log.info("✅ Customer receivable calculated: ৳ {}", receivable);
	            
	            return ResponseEntity.ok(response);
	            
	        } catch (Exception e) {
	            log.error("❌ Error calculating customer receivable: {}", e.getMessage(), e);
	            
	            Map<String, Object> error = new HashMap<>();
	            error.put("success", false);
	            error.put("message", "Error: " + e.getMessage());
	            error.put("totalReceivable", 0.0);
	            
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	        }
	    }

	    // ========================================
	    // 🏭 SUPPLIER LEDGER ENDPOINTS
	    // ========================================

	    /**
	     * 🔹 GET Supplier Ledger - All Transactions
	     * 
	     * Endpoint: GET /api/ledger/supplier
	     * Query Params:
	     *   - name (optional): Supplier name (supports partial search)
	     * 
	     * Example: GET /api/ledger/supplier?name=Mizan Agro
	     * Example: GET /api/ledger/supplier (returns all suppliers)
	     * 
	     * @param name Supplier name (optional)
	     * @return Complete supplier ledger
	     */
	    @GetMapping("/supplier")
	    public ResponseEntity<LedgerSummaryDTO> getSupplierLedger(
	            @RequestParam(required = false) String name) {
	        
	        log.info("🏭 REST API: Fetching supplier ledger for: {}", 
	                name != null ? name : "ALL SUPPLIERS");
	        
	        try {
	            LedgerSummaryDTO ledger = ledgerService.getSupplierLedger(name);
	            
	            log.info("✅ Supplier ledger retrieved successfully. Transactions: {}", 
	                    ledger.getTotalTransactions());
	            
	            return ResponseEntity.ok(ledger);
	            
	        } catch (Exception e) {
	            log.error("❌ Error fetching supplier ledger: {}", e.getMessage(), e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(LedgerSummaryDTO.empty("Error: " + e.getMessage()));
	        }
	    }

	    /**
	     * 🔹 GET Supplier Ledger - With Date Range Filter
	     * 
	     * Endpoint: GET /api/ledger/supplier/date-range
	     * Query Params:
	     *   - name (optional): Supplier name
	     *   - fromDate (optional): Start date
	     *   - toDate (optional): End date
	     * 
	     * Example: GET /api/ledger/supplier/date-range?name=Mizan&fromDate=2025-10-01T00:00:00&toDate=2025-10-31T23:59:59
	     * 
	     * @param name Supplier name (optional)
	     * @param fromDate Start date (optional)
	     * @param toDate End date (optional)
	     * @return Filtered supplier ledger
	     */
	    @GetMapping("/supplier/date-range")
	    public ResponseEntity<LedgerSummaryDTO> getSupplierLedgerByDateRange(
	            @RequestParam(required = false) String name,
	            @RequestParam(required = false) 
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
	            @RequestParam(required = false) 
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
	        
	        log.info("📅 REST API: Fetching supplier ledger - Name: {}, From: {}, To: {}", 
	                name != null ? name : "ALL", fromDate, toDate);
	        
	        try {
	            LedgerSummaryDTO ledger = ledgerService.getSupplierLedgerByDateRange(
	                    name, fromDate, toDate);
	            
	            log.info("✅ Supplier ledger (date range) retrieved. Transactions: {}", 
	                    ledger.getTotalTransactions());
	            
	            return ResponseEntity.ok(ledger);
	            
	        } catch (Exception e) {
	            log.error("❌ Error fetching supplier ledger by date: {}", e.getMessage(), e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(LedgerSummaryDTO.empty("Error: " + e.getMessage()));
	        }
	    }

	    /**
	     * 🔹 GET Supplier Total Payable
	     * 
	     * Endpoint: GET /api/ledger/supplier/payable
	     * Query Params:
	     *   - name (optional): Supplier name
	     * 
	     * Returns how much money the business owes to the supplier
	     * 
	     * Example: GET /api/ledger/supplier/payable?name=Mizan Agro
	     * 
	     * @param name Supplier name (optional)
	     * @return Total payable amount with status
	     */
	    @GetMapping("/supplier/payable")
	    public ResponseEntity<Map<String, Object>> getSupplierPayable(
	            @RequestParam(required = false) String name) {
	        
	        log.info("💸 REST API: Calculating supplier payable for: {}", 
	                name != null ? name : "ALL SUPPLIERS");
	        
	        try {
	            Double payable = ledgerService.getSupplierTotalPayable(name);
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("supplierName", name != null ? name : "All Suppliers");
	            response.put("totalPayable", payable);
	            response.put("formattedAmount", String.format("৳ %.2f", payable));
	            response.put("status", payable > 0 ? "DUE" : "CLEAR");
	            response.put("message", payable > 0 
	                    ? "Payment pending to supplier" 
	                    : "All payments cleared");
	            response.put("success", true);
	            
	            log.info("✅ Supplier payable calculated: ৳ {}", payable);
	            
	            return ResponseEntity.ok(response);
	            
	        } catch (Exception e) {
	            log.error("❌ Error calculating supplier payable: {}", e.getMessage(), e);
	            
	            Map<String, Object> error = new HashMap<>();
	            error.put("success", false);
	            error.put("message", "Error: " + e.getMessage());
	            error.put("totalPayable", 0.0);
	            
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	        }
	    }

	    // ========================================
	    // 📋 UTILITY ENDPOINTS
	    // ========================================

	    /**
	     * 🔹 GET All Customer Names (for dropdown/autocomplete)
	     * 
	     * Endpoint: GET /api/ledger/customers/names
	     * 
	     * Returns a list of all unique customer names in the system
	     * Useful for populating dropdowns in frontend
	     * 
	     * @return List of customer names
	     */
	    @GetMapping("/customers/names")
	    public ResponseEntity<Map<String, Object>> getAllCustomerNames() {
	        log.info("📋 REST API: Fetching all customer names");
	        
	        try {
	            List<String> names = ledgerService.getAllCustomerNames();
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("customers", names);
	            response.put("count", names.size());
	            response.put("success", true);
	            response.put("message", "Customer names retrieved successfully");
	            
	            log.info("✅ Retrieved {} customer names", names.size());
	            
	            return ResponseEntity.ok(response);
	            
	        } catch (Exception e) {
	            log.error("❌ Error fetching customer names: {}", e.getMessage(), e);
	            
	            Map<String, Object> error = new HashMap<>();
	            error.put("success", false);
	            error.put("message", "Error: " + e.getMessage());
	            error.put("customers", List.of());
	            error.put("count", 0);
	            
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	        }
	    }

	    /**
	     * 🔹 GET All Supplier Names (for dropdown/autocomplete)
	     * 
	     * Endpoint: GET /api/ledger/suppliers/names
	     * 
	     * Returns a list of all unique supplier names in the system
	     * Useful for populating dropdowns in frontend
	     * 
	     * @return List of supplier names
	     */
	    @GetMapping("/suppliers/names")
	    public ResponseEntity<Map<String, Object>> getAllSupplierNames() {
	        log.info("📋 REST API: Fetching all supplier names");
	        
	        try {
	            List<String> names = ledgerService.getAllSupplierNames();
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("suppliers", names);
	            response.put("count", names.size());
	            response.put("success", true);
	            response.put("message", "Supplier names retrieved successfully");
	            
	            log.info("✅ Retrieved {} supplier names", names.size());
	            
	            return ResponseEntity.ok(response);
	            
	        } catch (Exception e) {
	            log.error("❌ Error fetching supplier names: {}", e.getMessage(), e);
	            
	            Map<String, Object> error = new HashMap<>();
	            error.put("success", false);
	            error.put("message", "Error: " + e.getMessage());
	            error.put("suppliers", List.of());
	            error.put("count", 0);
	            
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	        }
	    }

	    /**
	     * 🔹 Health Check
	     * 
	     * Endpoint: GET /api/ledger/health
	     * 
	     * Simple endpoint to check if the service is running
	     * 
	     * @return Service status
	     */
	    @GetMapping("/health")
	    public ResponseEntity<Map<String, String>> healthCheck() {
	        log.info("❤️ Health check requested");
	        
	        Map<String, String> response = new HashMap<>();
	        response.put("status", "UP");
	        response.put("service", "Ledger Service");
	        response.put("version", "1.0.0");
	        response.put("timestamp", LocalDateTime.now().toString());
	        
	        return ResponseEntity.ok(response);
	    }

	    /**
	     * 🔹 API Info
	     * 
	     * Endpoint: GET /api/ledger/info
	     * 
	     * Returns information about available endpoints
	     * 
	     * @return API information
	     */
	    @GetMapping("/info")
	    public ResponseEntity<Map<String, Object>> apiInfo() {
	        log.info("ℹ️ API info requested");
	        
	        Map<String, Object> response = new HashMap<>();
	        response.put("serviceName", "Ledger Management API");
	        response.put("version", "1.0.0");
	        response.put("description", "Dynamic Customer & Supplier Ledger System");
	        
	        Map<String, String> endpoints = new HashMap<>();
	        endpoints.put("GET /api/ledger/customer", "Get customer ledger");
	        endpoints.put("GET /api/ledger/customer/date-range", "Get customer ledger with date filter");
	        endpoints.put("GET /api/ledger/customer/receivable", "Get customer receivable amount");
	        endpoints.put("GET /api/ledger/supplier", "Get supplier ledger");
	        endpoints.put("GET /api/ledger/supplier/date-range", "Get supplier ledger with date filter");
	        endpoints.put("GET /api/ledger/supplier/payable", "Get supplier payable amount");
	        endpoints.put("GET /api/ledger/customers/names", "Get all customer names");
	        endpoints.put("GET /api/ledger/suppliers/names", "Get all supplier names");
	        endpoints.put("GET /api/ledger/health", "Health check");
	        
	        response.put("endpoints", endpoints);
	        response.put("timestamp", LocalDateTime.now().toString());
	        
	        return ResponseEntity.ok(response);
	    }
	    
	    /**
	     * 🔹 GET Transaction Details by ID
	     * 
	     * Endpoint: GET /api/ledger/transaction/{id}
	     * 
	     * Returns detailed information about a specific transaction
	     * including related transactions
	     * 
	     * @param id Transaction ID
	     * @return Transaction details with related transactions
	     */
	    @GetMapping("/transaction/{id}")
	    public ResponseEntity<Map<String, Object>> getTransactionDetails(@PathVariable Long id) {
	        log.info("📄 REST API: Fetching transaction details for ID: {}", id);
	        
	        try {
	            Map<String, Object> response = ledgerService.getTransactionDetails(id);
	            
	            log.info("✅ Transaction details retrieved successfully");
	            
	            return ResponseEntity.ok(response);
	            
	        } catch (Exception e) {
	            log.error("❌ Error fetching transaction details: {}", e.getMessage(), e);
	            
	            Map<String, Object> error = new HashMap<>();
	            error.put("success", false);
	            error.put("message", "Error: " + e.getMessage());
	            
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	        }
	    }
	    
	    /**
	     * 🔹 GET Supplier Ledger Details (Complete Report)
	     * 
	     * Endpoint: GET /api/ledger/supplier/details
	     * Query Params:
	     *   - name: Supplier name
	     * 
	     * Returns complete supplier ledger with all transactions
	     * 
	     * @param name Supplier name
	     * @return Complete ledger summary with all transactions
	     */
	    /**
	     * Show Supplier Ledger Details (for modal/separate view)
	     */
	    @GetMapping("/supplier/details")
	    public String supplierLedgerDetails(
	            @RequestParam(required = false) String name,
	            Model model) {
	        
	        log.info("📄 Loading supplier ledger details page for: {}", name);
	        log.info("🔍 Request received at endpoint: /supplier/details");
	        
	        try {
	            // Fetch supplier ledger data
	            var ledgerData = ledgerService.getSupplierLedger(name);
	            
	            // Add data to model
	            model.addAttribute("supplierName", ledgerData.getEntityName());
	            model.addAttribute("entityType", ledgerData.getEntityType());
	            model.addAttribute("totalTransactions", ledgerData.getTotalTransactions());
	            model.addAttribute("fromDate", ledgerData.getFromDate());
	            model.addAttribute("toDate", ledgerData.getToDate());
	            model.addAttribute("currentBalance", ledgerData.getClosingBalance());
	            model.addAttribute("totalDebit", ledgerData.getTotalDebit());
	            model.addAttribute("totalCredit", ledgerData.getTotalCredit());
	            model.addAttribute("netBalance", ledgerData.getNetBalance());
	            model.addAttribute("transactions", ledgerData.getTransactions());
	            
	            // Current date
	            String currentDate = LocalDate.now().format(
	                    DateTimeFormatter.ofPattern("MMMM dd, yyyy")
	            );
	            model.addAttribute("currentDate", currentDate);
	            
	            log.info("✅ Supplier ledger details loaded successfully");
	            
	            // Return the correct view name
	            return "supplier_ledger_details";
	            
	        } catch (Exception e) {
	            log.error("❌ Error loading supplier ledger details: {}", e.getMessage(), e);
	            
	            model.addAttribute("error", "Error loading ledger details: " + e.getMessage());
	            return "error";
	        }
	    }
	    
	  
	    /**
	     * Show Customer Ledger Details (for modal/separate view)
	     */
	    @GetMapping("/customer/details")
	    public String customerLedgerDetails(
	            @RequestParam(required = false) String name,
	            Model model) {
	        
	        log.info("📄 Loading customer ledger details page for: {}", name);
	        
	        try {
	            // Fetch customer ledger data
	            var ledgerData = ledgerService.getCustomerLedger(name);
	            
	            // Add data to model
	            model.addAttribute("customerName", ledgerData.getEntityName());
	            model.addAttribute("entityType", ledgerData.getEntityType());
	            model.addAttribute("totalTransactions", ledgerData.getTotalTransactions());
	            model.addAttribute("fromDate", ledgerData.getFromDate());
	            model.addAttribute("toDate", ledgerData.getToDate());
	            model.addAttribute("currentBalance", ledgerData.getClosingBalance());
	            model.addAttribute("totalDebit", ledgerData.getTotalDebit());
	            model.addAttribute("totalCredit", ledgerData.getTotalCredit());
	            model.addAttribute("netBalance", ledgerData.getNetBalance());
	            model.addAttribute("transactions", ledgerData.getTransactions());
	            
	            // Current date
	            String currentDate = LocalDate.now().format(
	                    DateTimeFormatter.ofPattern("MMMM dd, yyyy")
	            );
	            model.addAttribute("currentDate", currentDate);
	            
	            log.info("✅ Customer ledger details loaded successfully");
	            
	            return "customer_ledger_details";
	            
	        } catch (Exception e) {
	            log.error("❌ Error loading customer ledger details: {}", e.getMessage(), e);
	            
	            model.addAttribute("error", "Error loading ledger details: " + e.getMessage());
	            return "error";
	        }
	    }
	}