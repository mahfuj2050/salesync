package com.business.salesync.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LedgerViewController {

    @GetMapping("/ledger/customer")
    public String customerLedgerPage() {
        return "fragments/customer_ledger";
    }

    @GetMapping("/ledger/supplier")
    public String supplierLedgerPage() {
        return "fragments/supplier_ledger";
    }
}