package com.business.salesync.dto;


import lombok.*;

//PurchaseOrderItemDTO.java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseOrderItemDTO {
 private String productName;
 private int quantity;
 private double purchasePrice;
 private double sellingPrice;
 private double subtotal;
 private double vatPercent;
 private double vatAmount;
 private double subtotalWithVat;
}
