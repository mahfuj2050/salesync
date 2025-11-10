package com.business.salesync.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.salesync.dto.ExpenseLedgerDTO;
import com.business.salesync.models.ExpenseLedger;
import com.business.salesync.service.ExpenseLedgerService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/expense-ledgers")
@Slf4j
public class ExpenseLedgerController {

    @Autowired
    private ExpenseLedgerService ledgerService;

    /**
     * Show all ledgers page
     */
    @GetMapping
    public String showAllLedgers(Model model) {
        model.addAttribute("categories", ExpenseLedger.LedgerCategory.values());
        model.addAttribute("ledgerTypes", ExpenseLedger.LedgerType.values());
        model.addAttribute("financialYears", getFinancialYears());
        return "fragments/expenseLedgers";
    }

    /**
     * Show create ledger form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        ExpenseLedgerDTO ledgerDTO = new ExpenseLedgerDTO();
        // Set default values
        ledgerDTO.setMonthlyBudget(BigDecimal.ZERO);
        ledgerDTO.setQuarterlyBudget(BigDecimal.ZERO);
        ledgerDTO.setYearlyBudget(BigDecimal.ZERO);
        ledgerDTO.setRequiresApproval(false);
        ledgerDTO.setEnableAlerts(true);
        ledgerDTO.setBudgetAlertThreshold(new BigDecimal("80.00"));
        ledgerDTO.setIsActive(true);
        
        model.addAttribute("ledger", ledgerDTO);
        model.addAttribute("categories", ExpenseLedger.LedgerCategory.values());
        model.addAttribute("ledgerTypes", ExpenseLedger.LedgerType.values());
        model.addAttribute("parentLedgers", ledgerService.getAllActiveLedgers());
        model.addAttribute("financialYears", getFinancialYears());
        return "fragments/expense_ledger_form";
    }
    /**
     * Show edit ledger form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            ExpenseLedgerDTO ledger = ledgerService.getLedgerById(id);
            model.addAttribute("ledger", ledger);
            model.addAttribute("categories", ExpenseLedger.LedgerCategory.values());
            model.addAttribute("ledgerTypes", ExpenseLedger.LedgerType.values());
            model.addAttribute("parentLedgers", ledgerService.getAllActiveLedgers());
            model.addAttribute("financialYears", getFinancialYears());
            return "expense_ledger_form";
        } catch (Exception e) {
            // Redirect to list if ledger not found
            return "redirect:/expense-ledgers";
        }
    }
    /**
     * Show categories page
     */
    @GetMapping("/categories")
    public String showCategories(Model model) {
        // Get category statistics
        Map<String, Long> categoryStats = ledgerService.getCategoryStatistics();
        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("categories", ExpenseLedger.LedgerCategory.values());
        return "fragments/expense_ledger_categories";
    }

    /**
     * Process form submission (for Thymeleaf form)
     */
    @PostMapping
    public String createLedgerFromForm(@ModelAttribute ExpenseLedgerDTO ledgerDTO, 
                                     RedirectAttributes redirectAttributes) {
        try {
            ExpenseLedgerDTO created = ledgerService.createLedger(ledgerDTO);
            redirectAttributes.addFlashAttribute("success", "Ledger created successfully!");
            return "redirect:/expense-ledgers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating ledger: " + e.getMessage());
            return "redirect:/expense-ledgers/new";
        }
    }

    /**
     * Process form update (for Thymeleaf form)
     */
    @PostMapping("/{id}")
    public String updateLedgerFromForm(@PathVariable Long id, 
                                     @ModelAttribute ExpenseLedgerDTO ledgerDTO,
                                     RedirectAttributes redirectAttributes) {
        try {
            ExpenseLedgerDTO updated = ledgerService.updateLedger(id, ledgerDTO);
            redirectAttributes.addFlashAttribute("success", "Ledger updated successfully!");
            return "redirect:/expense-ledgers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating ledger: " + e.getMessage());
            return "redirect:/expense-ledgers/edit/" + id;
        }
    }

    /**
     * Get financial years for dropdown
     */
    private List<String> getFinancialYears() {
        List<String> years = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = -2; i <= 2; i++) {
            years.add((currentYear + i) + "-" + (currentYear + i + 1));
        }
        return years;
    }
}