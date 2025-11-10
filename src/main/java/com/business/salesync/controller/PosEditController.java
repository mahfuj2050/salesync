package com.business.salesync.controller;


import com.business.salesync.models.*;
import com.business.salesync.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class PosEditController {

    @Autowired
    private OrderRepository saleOrderRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FinancialAccountRepository financialAccountRepository;

    @GetMapping("/edit-sale/{invoiceNumber}")
    public String editSale(@PathVariable("invoiceNumber") String invoiceNumber, Model model) {

        // Fetch sale order
        SalesOrder saleOrder = saleOrderRepository.findByInvoiceNumber(invoiceNumber)
                                    .orElse(null);

        if (saleOrder == null) {
            model.addAttribute("error", "Sale order not found");
            return "redirect:/sales";
        }

        // Fetch order details for this invoice
        List<OrderDetails> orderDetails = orderDetailsRepository.findByInvoiceNumber(invoiceNumber);

        // Optional: Payment info (if exists)
        Payment payment = paymentRepository.findByTrnRefNo(invoiceNumber);

        // Financial account for this invoice
        List<FinancialAccount> financialAccounts = financialAccountRepository.findAll(); // Show all for dropdown

        // Product list for "Add Product" dropdown
        List<Product> products = productRepository.findAll();

        // Format order date for input[type=date]
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedOrderDate = saleOrder.getDateOrdered().format(dtf);

        // Add attributes to model
        model.addAttribute("saleOrder", saleOrder);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("payment", payment);
        model.addAttribute("financialAccounts", financialAccounts);
        model.addAttribute("products", products);
        model.addAttribute("formattedOrderDate", formattedOrderDate);

        return "fragments/edit-sale"; // Thymeleaf template
    }

    @PostMapping("/update-sale")
    public String updateSale(@ModelAttribute SalesOrder saleOrder) {
        // TODO: update logic for saleOrder, orderDetails, payment, financialAccount
        saleOrderRepository.save(saleOrder);
        return "redirect:/sales";
    }
}
