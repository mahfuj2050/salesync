package com.business.salesync.controller;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.business.salesync.models.Expense;
import com.business.salesync.models.Expense.ExpenseCategory;
import com.business.salesync.models.Expense.PaymentStatus;
import com.business.salesync.models.ExpenseItem;
import com.business.salesync.repository.ExpenseItemRepository;
import com.business.salesync.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 🧾 Expense Controller
 * Handles all expense-related operations using repositories directly
 */
@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseItemRepository expenseItemRepository;

    // ========================================
    // 📄 VIEW PAGES
    // ========================================

    /**
     * Show expense list page
     * GET /expenses
     */
    @GetMapping
    public String expensesListPage(Model model) {
        log.info("📋 Loading expenses list page");
        
        // Add expense categories for filter dropdown
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        
        return "fragments/expenses";
    }

    /**
     * Show expense form page (Create new)
     * GET /expenses/new
     */
    @GetMapping("/new")
    public String expenseFormPage(Model model) {
        log.info("➕ Loading new expense form page");
        
        // Add enums for dropdowns
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("expenseTypes", Expense.ExpenseType.values());
        model.addAttribute("paymentMethods", getPaymentMethods());
        model.addAttribute("departments", getDepartments());
        
        // Add empty expense for form
        model.addAttribute("expense", new Expense());
        model.addAttribute("isEdit", false);
        
        return "fragments/expense_form";
    }

    /**
     * Show expense edit form page
     * GET /expenses/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String editExpenseFormPage(@PathVariable Long id, Model model) {
        log.info("✏️ Loading edit expense form for ID: {}", id);
        
        try {
            Optional<Expense> expenseOpt = expenseRepository.findById(id);
            
            if (expenseOpt.isEmpty()) {
                log.error("❌ Expense not found with ID: {}", id);
                model.addAttribute("error", "Expense not found");
                return "redirect:/expenses";
            }
            
            Expense expense = expenseOpt.get();
            
            // Add enums for dropdowns
            model.addAttribute("categories", ExpenseCategory.values());
            model.addAttribute("expenseTypes", Expense.ExpenseType.values());
            model.addAttribute("paymentMethods", getPaymentMethods());
            model.addAttribute("departments", getDepartments());
            
            model.addAttribute("expense", expense);
            model.addAttribute("isEdit", true);
            
            return "fragments/expense_form";
            
        } catch (Exception e) {
            log.error("❌ Error loading expense for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading expense");
            return "redirect:/expenses";
        }
    }

    /**
     * Show expense invoice in modal
     * GET /expenses/invoice/{id}
     */
    @GetMapping("/invoice/{id}")
    public String expenseInvoicePage(@PathVariable Long id, Model model) {
        log.info("📄 Loading expense invoice for ID: {}", id);
        
        try {
            Optional<Expense> expenseOpt = expenseRepository.findById(id);
            
            if (expenseOpt.isEmpty()) {
                log.error("❌ Expense not found with ID: {}", id);
                model.addAttribute("error", "Expense not found");
                return "error";
            }
            
            Expense expense = expenseOpt.get();
            List<ExpenseItem> items = expenseItemRepository.findByExpenseIdOrderById(id);
            
            model.addAttribute("expense", expense);
            model.addAttribute("expenseItems", items);
            model.addAttribute("currentDate", LocalDate.now().format(
                DateTimeFormatter.ofPattern("MMMM dd, yyyy")
            ));
            
            return "fragments/expense_invoice";
            
        } catch (Exception e) {
            log.error("❌ Error loading expense invoice: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading invoice");
            return "error";
        }
    }

    // ========================================
    // 🔌 REST API ENDPOINTS
    // ========================================

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String vendorName) {
        
        log.info("📊 Fetching expenses list - Category: {}, Status: {}", category, paymentStatus);
        
        try {
            List<Expense> expenses;
            
            // Apply filters if provided
            if (category != null || paymentStatus != null || fromDate != null || vendorName != null) {
                ExpenseCategory cat = category != null ? ExpenseCategory.valueOf(category) : null;
                PaymentStatus status = paymentStatus != null ? PaymentStatus.valueOf(paymentStatus) : null;
                LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
                LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;
                
                expenses = expenseRepository.searchExpenses(vendorName, cat, status, from, to);
                log.info("🔍 Using filtered query, found: {} expenses", expenses.size());
            } else {
                expenses = expenseRepository.findAll();
                log.info("📋 Using findAll(), found: {} expenses", expenses.size());
                
                // Debug: Log each expense
                for (Expense expense : expenses) {
                    log.info("💰 Expense: ID={}, RefNo={}, Amount={}, Date={}", 
                        expense.getId(), expense.getExpenseRefNo(), 
                        expense.getTotalAmount(), expense.getExpenseDate());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("expenses", expenses);
            response.put("count", expenses.size());
            
            log.info("✅ Retrieved {} expenses", expenses.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error fetching expenses: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get single expense by ID with items
     * GET /expenses/api/{id}
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExpenseById(@PathVariable Long id) {
        log.info("🔍 Fetching expense with ID: {}", id);
        
        try {
            Optional<Expense> expenseOpt = expenseRepository.findById(id);
            
            if (expenseOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Expense not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Expense expense = expenseOpt.get();
            List<ExpenseItem> items = expenseItemRepository.findByExpenseIdOrderById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("expense", expense);
            response.put("expenseItems", items);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error fetching expense: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Create new expense with items
     * POST /expenses/api/create
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createExpense(@RequestBody Map<String, Object> payload) {
        log.info("➕ Creating new expense");
        
        try {
            // Build expense entity from payload
            Expense expense = buildExpenseFromPayload(payload);
            
            // Generate expense reference number
            expense.setExpenseRefNo(generateExpenseRefNo());
            
            // Set audit fields
            expense.setCreatedAt(LocalDateTime.now());
            expense.setCreatedBy((String) payload.getOrDefault("createdBy", "admin"));
            
            // Save expense
            Expense savedExpense = expenseRepository.save(expense);
            
            // Save expense items if provided
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) payload.get("expenseItems");
            if (itemsData != null && !itemsData.isEmpty()) {
                for (Map<String, Object> itemData : itemsData) {
                    ExpenseItem item = buildExpenseItemFromPayload(itemData, savedExpense);
                    expenseItemRepository.save(item);
                }
            }
            
            // Recalculate totals
            List<ExpenseItem> items = expenseItemRepository.findByExpenseId(savedExpense.getId());
            recalculateExpenseTotals(savedExpense, items);
            savedExpense = expenseRepository.save(savedExpense);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expense created successfully");
            response.put("expense", savedExpense);
            response.put("expenseId", savedExpense.getId());
            response.put("expenseRefNo", savedExpense.getExpenseRefNo());
            
            log.info("✅ Expense created successfully: {}", savedExpense.getExpenseRefNo());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error creating expense: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error creating expense: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update existing expense
     * PUT /expenses/api/update/{id}
     */
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateExpense(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        
        log.info("✏️ Updating expense ID: {}", id);
        
        try {
            Optional<Expense> expenseOpt = expenseRepository.findById(id);
            
            if (expenseOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Expense not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Expense expense = expenseOpt.get();
            
            // Update fields from payload
            updateExpenseFromPayload(expense, payload);
            
            // Set update audit fields
            expense.setUpdatedAt(LocalDateTime.now());
            expense.setUpdatedBy((String) payload.getOrDefault("updatedBy", "admin"));
            
            // Save updated expense
            Expense updatedExpense = expenseRepository.save(expense);
            
            // Update expense items if provided
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) payload.get("expenseItems");
            if (itemsData != null) {
                // Delete existing items
                expenseItemRepository.deleteByExpenseId(id);
                
                // Create new items
                for (Map<String, Object> itemData : itemsData) {
                    ExpenseItem item = buildExpenseItemFromPayload(itemData, updatedExpense);
                    expenseItemRepository.save(item);
                }
            }
            
            // Recalculate totals
            List<ExpenseItem> items = expenseItemRepository.findByExpenseId(updatedExpense.getId());
            recalculateExpenseTotals(updatedExpense, items);
            updatedExpense = expenseRepository.save(updatedExpense);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expense updated successfully");
            response.put("expense", updatedExpense);
            
            log.info("✅ Expense updated successfully: {}", updatedExpense.getExpenseRefNo());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error updating expense: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error updating expense: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete expense (soft delete)
     * DELETE /expenses/api/delete/{id}
     */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteExpense(@PathVariable Long id) {
        log.info("🗑️ Deleting expense ID: {}", id);
        
        try {
            Optional<Expense> expenseOpt = expenseRepository.findById(id);
            
            if (expenseOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Expense not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Expense expense = expenseOpt.get();
            expense.setDeleted(true);
            expense.setUpdatedAt(LocalDateTime.now());
            expenseRepository.save(expense);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expense deleted successfully");
            
            log.info("✅ Expense deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error deleting expense: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error deleting expense: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Approve expense
     * POST /expenses/api/approve/{id}
     */
    @PostMapping("/api/approve/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveExpense(
            @PathVariable Long id,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String remarks) {
        
        log.info("✅ Approving expense ID: {} by {}", id, approvedBy);
        
        try {
            Optional<Expense> expenseOpt = expenseRepository.findById(id);
            
            if (expenseOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Expense not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Expense expense = expenseOpt.get();
            expense.setIsApproved(true);
            expense.setApprovedBy(approvedBy);
            expense.setApprovedAt(LocalDateTime.now());
            expense.setApprovalRemarks(remarks);
            
            Expense approvedExpense = expenseRepository.save(expense);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expense approved successfully");
            response.put("expense", approvedExpense);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error approving expense: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get expense statistics
     * GET /expenses/api/stats
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExpenseStats() {
        log.info("📊 Fetching expense statistics");
        
        try {
            BigDecimal totalExpenses = expenseRepository.getTotalExpensesByDateRange(
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now()
            );
            
            BigDecimal totalPending = expenseRepository.getTotalPendingPayments();
            
            long pendingCount = expenseRepository.countExpensesByStatus()
                .stream()
                .filter(obj -> "PENDING".equals(obj[0]))
                .mapToLong(obj -> (Long) obj[1])
                .sum();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalExpensesThisMonth", totalExpenses);
            stats.put("totalPendingPayments", totalPending);
            stats.put("pendingExpensesCount", pendingCount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error fetching stats: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ========================================
    // 🛠️ HELPER METHODS
    // ========================================

    private String generateExpenseRefNo() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        Optional<Expense> lastExpense = expenseRepository.findTopByOrderByIdDesc();
        long nextId = lastExpense.map(e -> e.getId() + 1).orElse(1L);
        
        return String.format("EXP-%s-%04d", dateStr, nextId);
    }

    private Expense buildExpenseFromPayload(Map<String, Object> payload) {
        Expense expense = new Expense();
        
        expense.setExpenseDate(LocalDate.parse((String) payload.get("expenseDate")));
        if (payload.get("dueDate") != null) {
            expense.setDueDate(LocalDate.parse((String) payload.get("dueDate")));
        }
        
        expense.setExpenseCategory(ExpenseCategory.valueOf((String) payload.get("expenseCategory")));
        if (payload.get("expenseType") != null) {
            expense.setExpenseType(Expense.ExpenseType.valueOf((String) payload.get("expenseType")));
        }
        
        expense.setVendorName((String) payload.get("vendorName"));
        expense.setVendorContact((String) payload.get("vendorContact"));
        expense.setDescription((String) payload.get("description"));
        expense.setReferenceNo((String) payload.get("referenceNo"));
        
        expense.setPaymentMethod((String) payload.get("paymentMethod"));
        expense.setDepartment((String) payload.get("department"));
        expense.setRemarks((String) payload.get("remarks"));
        expense.setFinancialYear((String) payload.getOrDefault("financialYear", "2025-2026"));
        
        expense.setPaymentStatus(PaymentStatus.PENDING);
        expense.setDeleted(false);
        
        return expense;
    }

    private void updateExpenseFromPayload(Expense expense, Map<String, Object> payload) {
        if (payload.containsKey("expenseDate")) {
            expense.setExpenseDate(LocalDate.parse((String) payload.get("expenseDate")));
        }
        if (payload.containsKey("vendorName")) {
            expense.setVendorName((String) payload.get("vendorName"));
        }
        if (payload.containsKey("description")) {
            expense.setDescription((String) payload.get("description"));
        }
        // Add other fields as needed
    }

    private ExpenseItem buildExpenseItemFromPayload(Map<String, Object> itemData, Expense expense) {
        ExpenseItem item = new ExpenseItem();
        item.setExpense(expense);
        item.setItemName((String) itemData.get("itemName"));
        item.setDescription((String) itemData.get("description"));
        item.setCategory((String) itemData.get("category"));
        
        item.setQuantity(new BigDecimal(itemData.get("quantity").toString()));
        item.setUnit((String) itemData.get("unit"));
        item.setUnitPrice(new BigDecimal(itemData.get("unitPrice").toString()));
        
        if (itemData.get("taxPercentage") != null) {
            item.setTaxPercentage(new BigDecimal(itemData.get("taxPercentage").toString()));
        }
        if (itemData.get("discount") != null) {
            item.setDiscount(new BigDecimal(itemData.get("discount").toString()));
        }
        
        item.calculateAmount();
        
        return item;
    }

    private void recalculateExpenseTotals(Expense expense, List<ExpenseItem> items) {
        BigDecimal subtotal = items.stream()
            .map(item -> item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal taxAmount = items.stream()
            .map(item -> item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discount = items.stream()
            .map(item -> item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal total = subtotal.add(taxAmount).subtract(discount);
        
        expense.setSubtotal(subtotal);
        expense.setTaxAmount(taxAmount);
        expense.setDiscount(discount);
        expense.setTotalAmount(total);
        expense.setAmountDue(total.subtract(expense.getAmountPaid() != null ? expense.getAmountPaid() : BigDecimal.ZERO));
        
        expense.updatePaymentStatus();
    }

    private List<String> getPaymentMethods() {
        return List.of("Cash", "Bank Transfer", "bKash", "Nagad", "Rocket", "Credit Card", "Cheque");
    }

    private List<String> getDepartments() {
        return List.of("Administration", "HR", "Finance", "IT", "Sales", "Marketing", "Operations", "Customer Support");
    }
}