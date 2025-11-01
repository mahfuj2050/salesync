package com.business.salesync.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.business.salesync.dto.ExpenseLedgerDTO;
import com.business.salesync.models.Expense;
import com.business.salesync.models.ExpenseLedger;
import com.business.salesync.repository.ExpenseLedgerRepository;
import com.business.salesync.repository.ExpenseRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ExpenseLedgerService {

    @Autowired
    private ExpenseLedgerRepository ledgerRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;

    /**
     * Create new expense ledger
     */
    public ExpenseLedgerDTO createLedger(ExpenseLedgerDTO dto) {
        // Validation
        if (ledgerRepository.existsByLedgerCode(dto.getLedgerCode())) {
            throw new RuntimeException("Ledger code already exists: " + dto.getLedgerCode());
        }
        
        if (ledgerRepository.existsByLedgerName(dto.getLedgerName())) {
            throw new RuntimeException("Ledger name already exists: " + dto.getLedgerName());
        }
        
        ExpenseLedger ledger = ExpenseLedger.builder()
            .ledgerCode(dto.getLedgerCode())
            .ledgerName(dto.getLedgerName())
            .ledgerCategory(ExpenseLedger.LedgerCategory.valueOf(dto.getLedgerCategory()))
            .ledgerType(ExpenseLedger.LedgerType.valueOf(dto.getLedgerType()))
            .description(dto.getDescription())
            .parentLedgerId(dto.getParentLedgerId())
            .monthlyBudget(dto.getMonthlyBudget())
            .quarterlyBudget(dto.getQuarterlyBudget())
            .yearlyBudget(dto.getYearlyBudget())
            .requiresApproval(dto.getRequiresApproval())
            .approvalLimit(dto.getApprovalLimit())
            .budgetAlertThreshold(dto.getBudgetAlertThreshold())
            .enableAlerts(dto.getEnableAlerts())
            .isActive(dto.getIsActive())
            .displayOrder(dto.getDisplayOrder())
            .icon(dto.getIcon())
            .colorCode(dto.getColorCode())
            .financialYear(dto.getFinancialYear())
            .createdBy(dto.getCreatedBy())
            .build();
        
        ExpenseLedger saved = ledgerRepository.save(ledger);
        log.info("Created expense ledger: {}", saved.getLedgerCode());
        
        return convertToDTO(saved);
    }

    /**
     * Update expense ledger
     */
    public ExpenseLedgerDTO updateLedger(Long id, ExpenseLedgerDTO dto) {
        ExpenseLedger existing = ledgerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ledger not found with id: " + id));
        
        // Check for duplicate name (excluding current record)
        if (!existing.getLedgerName().equals(dto.getLedgerName()) 
            && ledgerRepository.existsByLedgerName(dto.getLedgerName())) {
            throw new RuntimeException("Ledger name already exists: " + dto.getLedgerName());
        }
        
        existing.setLedgerName(dto.getLedgerName());
        existing.setLedgerCategory(ExpenseLedger.LedgerCategory.valueOf(dto.getLedgerCategory()));
        existing.setLedgerType(ExpenseLedger.LedgerType.valueOf(dto.getLedgerType()));
        existing.setDescription(dto.getDescription());
        existing.setParentLedgerId(dto.getParentLedgerId());
        existing.setMonthlyBudget(dto.getMonthlyBudget());
        existing.setQuarterlyBudget(dto.getQuarterlyBudget());
        existing.setYearlyBudget(dto.getYearlyBudget());
        existing.setRequiresApproval(dto.getRequiresApproval());
        existing.setApprovalLimit(dto.getApprovalLimit());
        existing.setBudgetAlertThreshold(dto.getBudgetAlertThreshold());
        existing.setEnableAlerts(dto.getEnableAlerts());
        existing.setIsActive(dto.getIsActive());
        existing.setDisplayOrder(dto.getDisplayOrder());
        existing.setIcon(dto.getIcon());
        existing.setColorCode(dto.getColorCode());
        existing.setFinancialYear(dto.getFinancialYear());
        existing.setUpdatedBy(dto.getUpdatedBy());
        
        ExpenseLedger updated = ledgerRepository.save(existing);
        log.info("Updated expense ledger: {}", updated.getLedgerCode());
        
        return convertToDTO(updated);
    }

    /**
     * Get ledger by ID
     */
    public ExpenseLedgerDTO getLedgerById(Long id) {
        ExpenseLedger ledger = ledgerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ledger not found with id: " + id));
        return convertToDTO(ledger);
    }

    /**
     * Get all active ledgers
     */
    public List<ExpenseLedgerDTO> getAllActiveLedgers() {
        return ledgerRepository.findAllActive().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get ledgers by category
     */
    public List<ExpenseLedgerDTO> getLedgersByCategory(String category) {
        ExpenseLedger.LedgerCategory ledgerCategory = ExpenseLedger.LedgerCategory.valueOf(category);
        return ledgerRepository.findByLedgerCategory(ledgerCategory).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Delete ledger (soft delete)
     */
    public void deleteLedger(Long id) {
        ExpenseLedger ledger = ledgerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ledger not found with id: " + id));
        
        ledger.setDeleted(true);
        ledgerRepository.save(ledger);
        log.info("Deleted expense ledger: {}", ledger.getLedgerCode());
    }

    /**
     * Get ledger with expense summary
     */
    public ExpenseLedgerDTO getLedgerWithSummary(Long id, LocalDate fromDate, LocalDate toDate) {
        ExpenseLedger ledger = ledgerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ledger not found with id: " + id));
        
        ExpenseLedgerDTO dto = convertToDTO(ledger);
        
        // Get expenses for this ledger
        List<Expense> expenses = expenseRepository.findByLedgerAndDateRange(id, fromDate, toDate);
        
        // Calculate summary
        BigDecimal totalExpenses = expenses.stream()
            .map(Expense::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPaid = expenses.stream()
            .map(Expense::getAmountPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalDue = expenses.stream()
            .map(Expense::getAmountDue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        dto.setTotalExpenses(totalExpenses);
        dto.setTotalPaid(totalPaid);
        dto.setTotalDue(totalDue);
        dto.setExpenseCount((long) expenses.size());
        
        // Calculate budget utilization
        if (ledger.getMonthlyBudget() != null && ledger.getMonthlyBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilization = totalExpenses
                .divide(ledger.getMonthlyBudget(), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            dto.setBudgetUtilization(utilization);
        }
        
        return dto;
    }

    /**
     * Convert Entity to DTO
     */
    private ExpenseLedgerDTO convertToDTO(ExpenseLedger ledger) {
        ExpenseLedgerDTO dto = ExpenseLedgerDTO.builder()
            .id(ledger.getId())
            .ledgerCode(ledger.getLedgerCode())
            .ledgerName(ledger.getLedgerName())
            .ledgerCategory(ledger.getLedgerCategory().name())
            .ledgerType(ledger.getLedgerType().name())
            .description(ledger.getDescription())
            .parentLedgerId(ledger.getParentLedgerId())
            .monthlyBudget(ledger.getMonthlyBudget())
            .quarterlyBudget(ledger.getQuarterlyBudget())
            .yearlyBudget(ledger.getYearlyBudget())
            .requiresApproval(ledger.getRequiresApproval())
            .approvalLimit(ledger.getApprovalLimit())
            .budgetAlertThreshold(ledger.getBudgetAlertThreshold())
            .enableAlerts(ledger.getEnableAlerts())
            .isActive(ledger.getIsActive())
            .displayOrder(ledger.getDisplayOrder())
            .icon(ledger.getIcon())
            .colorCode(ledger.getColorCode())
            .financialYear(ledger.getFinancialYear())
            .createdAt(ledger.getCreatedAt())
            .createdBy(ledger.getCreatedBy())
            .updatedAt(ledger.getUpdatedAt())
            .updatedBy(ledger.getUpdatedBy())
            .build();
        
        // Get parent ledger name if exists
        if (ledger.getParentLedgerId() != null) {
            ledgerRepository.findById(ledger.getParentLedgerId())
                .ifPresent(parent -> dto.setParentLedgerName(parent.getLedgerName()));
        }
        
        return dto;
    }
    
    public Map<String, Long> getCategoryStatistics() {
        List<ExpenseLedger> allLedgers = ledgerRepository.findAllActive();
        return allLedgers.stream()
            .collect(Collectors.groupingBy(
                ledger -> ledger.getLedgerCategory().name(),
                Collectors.counting()
            ));
    }
}