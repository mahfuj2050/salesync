package com.business.salesync.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemDTO {
    private Long productId;
    private int quantity;
    private double returnAmount;
}