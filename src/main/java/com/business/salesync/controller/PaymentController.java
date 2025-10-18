package com.business.salesync.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.salesync.models.Payment;
import com.business.salesync.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @GetMapping
    public String listPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String refType,
            @RequestParam(required = false) String paymentStatus,
            Model model) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // ✅ Ensure nulls are actually null, not empty strings
        search = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        refType = (refType != null && !refType.isEmpty()) ? refType : null;
        paymentStatus = (paymentStatus != null && !paymentStatus.isEmpty()) ? paymentStatus : null;

        Page<Payment> paymentsPage;

        Payment.RefType refTypeEnum = (refType != null) ? Payment.RefType.valueOf(refType) : null;
        Payment.PaymentStatus statusEnum = (paymentStatus != null) ? Payment.PaymentStatus.valueOf(paymentStatus) : null;

        if (search != null || refTypeEnum != null || statusEnum != null) {
            paymentsPage = paymentRepository.filterPayments(search, refTypeEnum, statusEnum, pageable);
        } else {
            paymentsPage = paymentRepository.findAll(pageable);
        }

        model.addAttribute("payments", paymentsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paymentsPage.getTotalPages());
        model.addAttribute("totalItems", paymentsPage.getTotalElements());
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("search", search);
        
        //model.addAttribute("refType", refType);
        //model.addAttribute("paymentStatus", paymentStatus);
        //model.addAttribute("refTypes", Payment.RefType.values());
        //model.addAttribute("paymentStatuses", Payment.PaymentStatus.values());
        //model.addAttribute("totalReceived", paymentRepository.getTotalAmountPaidAll());
        //model.addAttribute("totalDue", paymentRepository.getTotalAmountDueAll());
        
        model.addAttribute("refType", Payment.RefType.values());
        model.addAttribute("paymentStatuses", Payment.PaymentStatus.values());
        model.addAttribute("totalReceived", paymentRepository.getTotalAmountPaidAll());
        model.addAttribute("totalDue", paymentRepository.getTotalAmountDueAll());


        return "fragments/payments";
    }


    // Show payment form for creating new payment
    @GetMapping("/new")
    public String showPaymentForm(Model model) {
        model.addAttribute("payment", Payment.builder().build());
        model.addAttribute("refTypes", Payment.RefType.values());
        model.addAttribute("paymentStatuses", Payment.PaymentStatus.values());
        return "fragments/payment_form";
    }

    // Show payment form for editing
    @GetMapping("/edit/{id}")
    public String editPayment(@PathVariable Long id, Model model) {
        Optional<Payment> payment = paymentRepository.findById(id);
        if (payment.isPresent()) {
            model.addAttribute("payment", payment.get());
            model.addAttribute("refTypes", Payment.RefType.values());
            model.addAttribute("paymentStatuses", Payment.PaymentStatus.values());
            return "payments/payment_form";
        }
        return "redirect:/payments";
    }

    // Save payment (create or update)
	/*-
    @PostMapping("/save")
    public String savePayment(@ModelAttribute Payment payment, RedirectAttributes redirectAttributes) {
        try {
            // Auto-calculate amount due if not set
            if (payment.getAmountDue() == 0 && payment.getGrandTotal() > 0) {
                payment.setAmountDue(payment.getGrandTotal() - payment.getAmountPaid());
            }

            // Auto-update payment status
            payment.updatePaymentStatus(payment.getAmountPaid());

            paymentRepository.save(payment);
            redirectAttributes.addFlashAttribute("successMessage", "Payment saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving payment: " + e.getMessage());
        }
        return "redirect:/payments";
    }*/

    // Delete payment
    @GetMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            paymentRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Payment deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting payment: " + e.getMessage());
        }
        return "redirect:/payments";
    }

    // View payment details (for modal)
    @GetMapping("/details/{id}")
    @ResponseBody
    public Payment getPaymentDetails(@PathVariable Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    // Create payment for specific invoice/order
    @GetMapping("/create-for-invoice/{refId}/{refType}")
    public String createPaymentForInvoice(
            @PathVariable Long refId,
            @PathVariable String refType,
            Model model) {
        
        Payment payment = Payment.builder()
                .refId(refId)
                .refType(Payment.RefType.valueOf(refType))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(Payment.PaymentStatus.PENDING)
                .build();
        
        model.addAttribute("payment", payment);
        model.addAttribute("refTypes", Payment.RefType.values());
        model.addAttribute("paymentStatuses", Payment.PaymentStatus.values());
        return "fragments/payment_form";
    }
}