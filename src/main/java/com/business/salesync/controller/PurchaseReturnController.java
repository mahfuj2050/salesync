package com.business.salesync.controller;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.salesync.models.PurchaseReturn;
import com.business.salesync.repository.ProductRepository;
import com.business.salesync.repository.PurchaseReturnItemRepository;
import com.business.salesync.repository.PurchaseReturnRepository;
import com.business.salesync.repository.SupplierRepository;
import com.business.salesync.service.FinancialAccountService;
import com.business.salesync.service.ReturnService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/purchase-return")
@RequiredArgsConstructor
public class PurchaseReturnController {

    private final PurchaseReturnRepository purchaseReturnRepo;
    private final PurchaseReturnItemRepository purchaseReturnItemRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;
    private final FinancialAccountService financialAccountService;
    private final ReturnService returnService;

    /** ✅ Show all Purchase Returns (list view) */
    @GetMapping
    public String listPurchaseReturns(Model model) {
        List<PurchaseReturn> purchaseReturns = purchaseReturnRepo.findAll();
        model.addAttribute("purchaseReturns", purchaseReturns != null ? purchaseReturns : Collections.emptyList());

        // Optional summary cards
        model.addAttribute("totalReturns", purchaseReturns.size());
        double totalValue = purchaseReturns.stream()
                .mapToDouble(pr -> pr.getTotalReturnAmount() != null ? pr.getTotalReturnAmount() : 0)
                .sum();
        model.addAttribute("totalReturnValue", totalValue);
        model.addAttribute("supplierCount", supplierRepo.count());

        // Suppliers for filter
        model.addAttribute("suppliers", supplierRepo.findAll());
        return "fragments/purchase-returns"; // Thymeleaf template
    }


    /** ✅ Load new Purchase Return form */
    @GetMapping("/new")
    public String newPurchaseReturn(Model model) {
        model.addAttribute("purchaseReturn", new PurchaseReturn());
        model.addAttribute("suppliers", supplierRepo.findAll());
        model.addAttribute("products", productRepo.findAll());
        return "fragments/purchase-return-form"; // form page
    }

    /** ✅ Save Purchase Return */
    @PostMapping("/save")
    public String savePurchaseReturn(@ModelAttribute PurchaseReturn purchaseReturn,
                                     RedirectAttributes redirectAttributes) {

        // Auto timestamp and reference number
        purchaseReturn.setReturnDate(LocalDateTime.now());
        if (purchaseReturn.getReturnRefNo() == null || purchaseReturn.getReturnRefNo().isBlank()) {
            purchaseReturn.setReturnRefNo("PR-" + System.currentTimeMillis());
        }

        // Link items with parent entity
        purchaseReturn.getItems().forEach(item -> item.setPurchaseReturn(purchaseReturn));

        // Calculate total return amount
        double totalReturn = purchaseReturn.getItems().stream()
                .mapToDouble(i -> i.getUnitCost() * i.getQuantityReturned())
                .sum();
        purchaseReturn.setTotalReturnAmount(totalReturn);

        // Save main and child entities
        PurchaseReturn saved = purchaseReturnRepo.save(purchaseReturn);
        purchaseReturnItemRepo.saveAll(purchaseReturn.getItems());

        // ✅ Delegate stock, ledger, and financial transaction logic
        returnService.processPurchaseReturn(saved);

        redirectAttributes.addFlashAttribute("success", "Purchase return saved successfully!");
        return "redirect:/purchase-return/list";
    }

    /** ✅ Optional: View details of a specific Purchase Return */
    @GetMapping("/details/{id}")
    public String viewPurchaseReturnDetails(@PathVariable Long id, Model model) {
        PurchaseReturn pr = purchaseReturnRepo.findById(id).orElseThrow();
        model.addAttribute("purchaseReturn", pr);
        return "fragments/purchase-return-details :: fragment";
    }

}
