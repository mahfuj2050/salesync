package com.business.salesync.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Service;

import com.business.salesync.models.FinancialAccount;
import com.business.salesync.repository.FinancialAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinancialAccountService {

    private final FinancialAccountRepository financialAccountRepository;

    /**
     * Save financial transaction for any type: SALE, PURCHASE, EXPENSE, PAYMENT
     */
    public FinancialAccount recordTransaction(
            String finAccName,
            String finAccType,
            Double amount,
            String transactionType, // "CASH_IN" or "CASH_OUT"
            String refType,         // "SALE_ORDER", "PURCHASE_ORDER", "EXPENSE", "PAYMENT"
            String entityType,      // "CUSTOMER", "SUPPLIER", etc.
            String entityName,
            String trnRefNo,
            Long refId,
            String remarks,
            String paymentStatus
    ) {
    	List<FinancialAccount> accounts = financialAccountRepository.findByFinAccName(finAccName);
    	if (accounts.isEmpty()) {
    	    throw new RuntimeException("Account not found: " + finAccName);
    	}
    	FinancialAccount account = accounts.get(0); // or handle multiple accounts as needed


        double openingBalance = account.getCurrentBalance();
        double debit = 0.0;
        double credit = 0.0;

        if ("CASH_IN".equalsIgnoreCase(transactionType)) {
            debit = amount;
        } else {
            credit = amount;
        }

        double newBalance = openingBalance + debit - credit;

        FinancialAccount entry = FinancialAccount.builder()
                .finAccId(account.getFinAccId())
                .finAccName(account.getFinAccName())
                .finAccType(account.getFinAccType())
                .openingBalance(openingBalance)
                .debitAmount(debit)
                .creditAmount(credit)
                .balanceAfterTransaction(newBalance)
                .currentBalance(newBalance)
                .trnDate(LocalDateTime.now())
                .trnRefNo(trnRefNo)
                .refId(refId)
                .refType(refType)
                .transactionType(transactionType)
                .entityType(entityType)
                .entityName(entityName)
                .paymentMethod(account.getFinAccType())
                .financialYear(Year.now().toString())
                .paymentStatus(paymentStatus)
                .remarks(remarks)
                .isPosted(true)
                .createdBy("SYSTEM")
                .build();

        // Save the new entry
        FinancialAccount saved = financialAccountRepository.save(entry);

        // Update main account balance (if needed)
        account.setCurrentBalance(newBalance);
        financialAccountRepository.save(account);

        return saved;
    }
}
