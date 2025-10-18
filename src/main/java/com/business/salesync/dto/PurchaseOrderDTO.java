package com.business.salesync.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseOrderDTO {
 private Long id;
 private String purchaseOrderNo;
 private String supplierName;
 private LocalDate insertDate;
 private LocalDate updateDate;
 private double totalAmount;
 private BigDecimal vatAmount;
 private double discount;
 private double grandTotal;
 private double amountPaid;
 private double amountDue;
 private String paymentStatus;
 private String status;
 private String remarks;

 private List<PurchaseOrderItemDTO> items;
}
