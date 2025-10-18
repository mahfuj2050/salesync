package com.business.salesync.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.business.salesync.models.PurchaseOrder;
import com.business.salesync.repository.PurchaseOrderRepository;

@Service
public class PONumberService {
    
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepo;
    
    public String generateNextPONumber() {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String basePrefix = "PO-" + today + "-";
        
        // Find the highest PO number for today
        List<PurchaseOrder> todayPOs = purchaseOrderRepo.findByPurchaseOrderNoContainingIgnoreCase(basePrefix);
        
        if (todayPOs.isEmpty()) {
            return basePrefix + "001";
        }
        
        // Find the maximum sequence number for today
        int maxSequence = todayPOs.stream()
            .map(po -> po.getPurchaseOrderNo())
            .map(poNo -> {
                try {
                    return Integer.parseInt(poNo.substring(poNo.lastIndexOf("-") + 1));
                } catch (NumberFormatException e) {
                    return 0;
                }
            })
            .max(Integer::compareTo)
            .orElse(0);
        
        return basePrefix + String.format("%03d", maxSequence + 1);
    }
}