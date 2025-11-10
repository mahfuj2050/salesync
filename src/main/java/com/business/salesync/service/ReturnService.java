package com.business.salesync.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.business.salesync.dto.ReturnItemDTO;
import com.business.salesync.models.*;
import com.business.salesync.service.*;
import com.business.salesync.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.time.Year;


@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ProductRepository productRepo;
    private final CustomerLedgerRepository customerLedgerRepo;
    private final SupplierLedgerRepository supplierLedgerRepo;
    private final FinancialAccountService financialAccountService;

    /** ✅ Handles Sales Returns (Customer → Company) */
    public void processSalesReturn(SalesReturn salesReturn) {
        BigDecimal totalReturn = BigDecimal.ZERO;

        for (SalesReturnItem item : salesReturn.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                int newStock = product.getQuantity() + item.getQuantityReturned();
                product.setQuantity(newStock);
                productRepo.save(product);
            }
            totalReturn = totalReturn.add(BigDecimal.valueOf(item.getUnitPrice() * item.getQuantityReturned()));
        }

        // ✅ Record in Customer Ledger
        CustomerLedger ledger = new CustomerLedger();
        ledger.setCustomer(salesReturn.getCustomer());
        ledger.setTrnDate(LocalDateTime.now());
        ledger.setTrnRefNo(salesReturn.getReturnRefNo());
        ledger.setTrnType("SALE_RETURN");
        ledger.setDebitAmount(0.0);
        ledger.setCreditAmount(totalReturn.doubleValue());
        ledger.setRemarks("Sales return processed");
        customerLedgerRepo.save(ledger);

        // ✅ Record Financial Transaction
        financialAccountService.recordTransaction(
                "Main Cash Account",       // finAccName
                "CASH",                    // finAccType
                totalReturn.doubleValue(), // amount
                "CASH_OUT",                // transactionType
                "SALE_RETURN",             // refType
                "CUSTOMER",                // entityType
                salesReturn.getCustomer() != null ? salesReturn.getCustomer().getName() : "Customer Return",
                salesReturn.getReturnRefNo(), // trnRefNo
                salesReturn.getId(),          // refId
                "Sales return refund to customer", // remarks
                "COMPLETED"                   // paymentStatus
        );
    }

    /** ✅ Handles Purchase Returns (Company → Supplier) */
    public void processPurchaseReturn(PurchaseReturn purchaseReturn) {
        BigDecimal totalReturn = BigDecimal.ZERO;

        for (PurchaseReturnItem item : purchaseReturn.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                int newStock = product.getQuantity() - item.getQuantityReturned();
                product.setQuantity(newStock);
                productRepo.save(product);
            }
            totalReturn = totalReturn.add(BigDecimal.valueOf(item.getUnitCost() * item.getQuantityReturned()));
        }

        // ✅ Record in Supplier Ledger
        SupplierLedger ledger = new SupplierLedger();
        ledger.setSupplier(purchaseReturn.getSupplier());
        ledger.setTrnDate(LocalDateTime.now());
        ledger.setTrnRefNo(purchaseReturn.getReturnRefNo());
        ledger.setTrnType("PURCHASE_RETURN");
        ledger.setDebitAmount(totalReturn.doubleValue());
        ledger.setCreditAmount(0.0);
        ledger.setRemarks("Purchase return processed");
        supplierLedgerRepo.save(ledger);

        // ✅ Record Financial Transaction
        financialAccountService.recordTransaction(
                "Main Cash Account",          // finAccName
                "CASH",                       // finAccType
                totalReturn.doubleValue(),    // amount
                "CASH_IN",                    // transactionType
                "PURCHASE_RETURN",            // refType
                "SUPPLIER",                   // entityType
                purchaseReturn.getSupplier() != null ? purchaseReturn.getSupplier().getSupplierName() : "Supplier Return",
                purchaseReturn.getReturnRefNo(), // trnRefNo
                purchaseReturn.getId(),          // refId
                "Purchase return refund from supplier", // remarks
                "COMPLETED"                    // paymentStatus
        );
    }
}
