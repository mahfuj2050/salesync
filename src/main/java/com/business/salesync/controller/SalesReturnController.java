package com.business.salesync.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.salesync.models.SalesReturn;
import com.business.salesync.repository.CustomerRepository;
import com.business.salesync.repository.FinancialAccountRepository;
import com.business.salesync.repository.ProductRepository;
import com.business.salesync.repository.SalesReturnItemRepository;
import com.business.salesync.repository.SalesReturnRepository;
import com.business.salesync.service.FinancialAccountService;
import com.business.salesync.service.ReturnService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/sales-return")
@RequiredArgsConstructor
public class SalesReturnController {

    private final SalesReturnRepository salesReturnRepo;
    private final SalesReturnItemRepository salesReturnItemRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final FinancialAccountRepository financialAccountRepository;
    private final FinancialAccountService financialAccountService;
    private final ReturnService returnService;

    /** ✅ Show all Sales Returns (list view) */
    @GetMapping
    public String listSalesReturns(Model model) {
        // Fetch all sales return records
        List<SalesReturn> salesReturns = salesReturnRepo.findAll();

        // Summary Calculations
        long totalReturns = salesReturns.size(); // total number of return records

        double totalReturnValue = salesReturns.stream()
                .mapToDouble(sr -> sr.getTotalReturnAmount() != null ? sr.getTotalReturnAmount() : 0.0)
                .sum(); // sum of all return amounts

        long totalCustomers = salesReturns.stream()
                .map(sr -> sr.getCustomer() != null ? sr.getCustomer().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count(); // number of unique customers

        // Add to model for Thymeleaf
        model.addAttribute("salesReturns", salesReturns);
        model.addAttribute("totalReturns", totalReturns);
        model.addAttribute("totalReturnValue", totalReturnValue);
        model.addAttribute("totalCustomers", totalCustomers);

        return "fragments/sales-returns"; // Thymeleaf view page
    }


    /** ✅ Load new Sales Return form */
    @GetMapping("/new")
    public String newSalesReturn(Model model) {
        model.addAttribute("salesReturn", new SalesReturn());
        model.addAttribute("customers", customerRepo.findAll());
        model.addAttribute("products", productRepo.findAll());
        return "fragments/sales-return-form"; // form page
    }

    /** ✅ Save Sales Return */
    @PostMapping("/save")
    public String saveSalesReturn(@ModelAttribute SalesReturn salesReturn,
                                  RedirectAttributes redirectAttributes) {

        // Auto timestamp and reference number
        salesReturn.setReturnDate(LocalDateTime.now());
        if (salesReturn.getReturnRefNo() == null || salesReturn.getReturnRefNo().isBlank()) {
            salesReturn.setReturnRefNo("SR-" + System.currentTimeMillis());
        }

        // Link items to parent entity
        salesReturn.getItems().forEach(item -> item.setSalesReturn(salesReturn));

        // Calculate total
        double totalReturn = salesReturn.getItems().stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantityReturned())
                .sum();
        salesReturn.setTotalReturnAmount(totalReturn);

        // Save main record and items
        SalesReturn saved = salesReturnRepo.save(salesReturn);
        salesReturnItemRepo.saveAll(salesReturn.getItems());

        // ✅ Delegate stock + ledger + financial logic to ReturnService
        returnService.processSalesReturn(saved);

        redirectAttributes.addFlashAttribute("success", "Sales return saved successfully!");
        return "redirect:/sales-return/list";
    }

    /** ✅ Optional: View a single Sales Return detail */
    /** View a single Sales Return detail */
    @GetMapping("/details/{id}")
    public String viewSalesReturnDetails(@PathVariable Long id, Model model) {
        SalesReturn sr = salesReturnRepo.findById(id)
                            .orElseThrow(() -> new RuntimeException("SalesReturn not found: " + id));
        model.addAttribute("salesReturn", sr);
        return "fragments/sales-return-details"; // full Thymeleaf template
    }


}

