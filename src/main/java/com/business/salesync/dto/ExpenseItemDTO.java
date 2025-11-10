package com.business.salesync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 🧾 Expense Item DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseItemDTO {
    
    private Long id;
    private String itemName;
    private String description;
    private String category;
    
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal discount;
    private BigDecimal total;
    
    private String serialNumber;
    private LocalDate warrantyExpiry;
    private String remarks;
    
    private Boolean isAsset;
    private Long assetId;
}