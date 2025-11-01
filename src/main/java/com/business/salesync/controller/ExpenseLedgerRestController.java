package com.business.salesync.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.business.salesync.dto.ExpenseLedgerDTO;
import com.business.salesync.models.ExpenseLedger;
import com.business.salesync.service.ExpenseLedgerService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/expense-ledgers")
@Slf4j
public class ExpenseLedgerRestController {

    @Autowired
    private ExpenseLedgerService ledgerService;

    /**
     * Create new ledger
     */
    @PostMapping
    public ResponseEntity<ExpenseLedgerDTO> createLedger(@RequestBody ExpenseLedgerDTO dto) {
        ExpenseLedgerDTO created = ledgerService.createLedger(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update ledger
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseLedgerDTO> updateLedger(
            @PathVariable Long id,
            @RequestBody ExpenseLedgerDTO dto) {
        ExpenseLedgerDTO updated = ledgerService.updateLedger(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get ledger by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseLedgerDTO> getLedger(@PathVariable Long id) {
        ExpenseLedgerDTO ledger = ledgerService.getLedgerById(id);
        return ResponseEntity.ok(ledger);
    }

    /**
     * Get all active ledgers
     */
    @GetMapping
    public ResponseEntity<List<ExpenseLedgerDTO>> getAllLedgers() {
        List<ExpenseLedgerDTO> ledgers = ledgerService.getAllActiveLedgers();
        return ResponseEntity.ok(ledgers);
    }

    /**
     * Get ledgers by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ExpenseLedgerDTO>> getLedgersByCategory(@PathVariable String category) {
        List<ExpenseLedgerDTO> ledgers = ledgerService.getLedgersByCategory(category);
        return ResponseEntity.ok(ledgers);
    }

    /**
     * Delete ledger
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLedger(@PathVariable Long id) {
        ledgerService.deleteLedger(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get ledger with expense summary
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<ExpenseLedgerDTO> getLedgerWithSummary(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        ExpenseLedgerDTO ledger = ledgerService.getLedgerWithSummary(id, fromDate, toDate);
        return ResponseEntity.ok(ledger);
    }
    
    
 // In your ExpenseLedgerController or a separate controller

    /**
     * Show create ledger form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("ledger", new ExpenseLedgerDTO());
        model.addAttribute("categories", ExpenseLedger.LedgerCategory.values());
        model.addAttribute("ledgerTypes", ExpenseLedger.LedgerType.values());
        model.addAttribute("parentLedgers", ledgerService.getAllActiveLedgers());
        model.addAttribute("financialYears", getFinancialYears());
        return "expense_ledger_form";
    }

    /**
     * Show edit ledger form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        ExpenseLedgerDTO ledger = ledgerService.getLedgerById(id);
        model.addAttribute("ledger", ledger);
        model.addAttribute("categories", ExpenseLedger.LedgerCategory.values());
        model.addAttribute("ledgerTypes", ExpenseLedger.LedgerType.values());
        model.addAttribute("parentLedgers", ledgerService.getAllActiveLedgers());
        model.addAttribute("financialYears", getFinancialYears());
        return "expense_ledger_form";
    }

    /**
     * Show categories page
     */
    @GetMapping("/categories")
    public String showCategories(Model model) {
        // Add logic to show ledger categories
        return "expense_ledger_categories";
    }

    /**
     * Get financial years for dropdown
     */
    private List<String> getFinancialYears() {
        // Implement logic to generate financial years
        List<String> years = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = -2; i <= 2; i++) {
            years.add((currentYear + i) + "-" + (currentYear + i + 1));
        }
        return years;
    }
}