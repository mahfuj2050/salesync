package com.business.salesync.service;

import org.springframework.stereotype.Service;

import com.business.salesync.models.Customer;
import com.business.salesync.models.CustomerLedger;
import com.business.salesync.models.Supplier;
import com.business.salesync.models.SupplierLedger;
import com.business.salesync.repository.CustomerLedgerRepository;
import com.business.salesync.repository.SupplierLedgerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final CustomerLedgerRepository customerLedgerRepository;
    private final SupplierLedgerRepository supplierLedgerRepository;

    public void recordCustomerTransaction(Customer customer, String refNo, String type,
                                          Double debit, Double credit, String remarks) {
        double lastBalance = customerLedgerRepository
            .findByCustomerIdOrderByTrnDateAsc(customer.getId())
            .stream()
            .mapToDouble(CustomerLedger::getBalanceAfter)
            .reduce((a, b) -> b).orElse(0);

        double newBalance = lastBalance + (debit - credit);

        CustomerLedger ledger = CustomerLedger.builder()
                .customer(customer)
                .trnRefNo(refNo)
                .trnType(type)
                .debitAmount(debit)
                .creditAmount(credit)
                .balanceAfter(newBalance)
                .remarks(remarks)
                .build();

        customerLedgerRepository.save(ledger);
    }

    public void recordSupplierTransaction(Supplier supplier, String refNo, String type,
                                          Double debit, Double credit, String remarks) {
        double lastBalance = supplierLedgerRepository
            .findBySupplierIdOrderByTrnDateAsc(supplier.getId())
            .stream()
            .mapToDouble(SupplierLedger::getBalanceAfter)
            .reduce((a, b) -> b).orElse(0);

        double newBalance = lastBalance + (debit - credit);

        SupplierLedger ledger = SupplierLedger.builder()
                .supplier(supplier)
                .trnRefNo(refNo)
                .trnType(type)
                .debitAmount(debit)
                .creditAmount(credit)
                .balanceAfter(newBalance)
                .remarks(remarks)
                .build();

        supplierLedgerRepository.save(ledger);
    }
}
