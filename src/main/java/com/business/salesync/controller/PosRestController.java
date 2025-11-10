package com.business.salesync.controller;

// --- Standard Java Imports (Organized) ---
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// --- Spring Framework Imports ---
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// --- Third-Party/Utility Imports ---
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// --- Application Model and Repository Imports ---
import com.business.salesync.models.HeldInvoice;
import com.business.salesync.models.Payment;
import com.business.salesync.models.SalesOrder;
import com.business.salesync.models.Payment.RefType;
import com.business.salesync.repository.HeldInvoiceRepository;
import com.business.salesync.repository.OrderRepository;
import com.business.salesync.repository.PaymentRepository;

@Slf4j
@RestController
@RequestMapping("/pos")
@RequiredArgsConstructor // Injects final fields via constructor
public class PosRestController {

    // --- Repositories (Injected via Constructor) ---
    private final HeldInvoiceRepository heldInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    // ----------------------------------------------------------------------
    //                           HELD INVOICE ENDPOINTS
    // ----------------------------------------------------------------------

    /**
     * Creates a new held invoice record.
     * Maps to: POST /pos/hold
     */
    @PostMapping("/hold")
    public ResponseEntity<String> holdInvoice(@RequestBody HeldInvoice invoice) {
        try {
            invoice.setStatus("HELD");
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setUpdatedAt(LocalDateTime.now());
            heldInvoiceRepository.save(invoice);
            log.info("Invoice held successfully: {}", invoice.getId());
            return ResponseEntity.ok("Invoice held successfully!");
        } catch (Exception e) {
            log.error("Failed to hold invoice.", e);
            // Return JSON-formatted error body for consistency in a REST controller
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to hold invoice\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Retrieves all invoices with status 'HELD'.
     * Maps to: GET /pos/held
     */
    @GetMapping("/held")
    public ResponseEntity<List<HeldInvoice>> viewHeldInvoices() {
        return ResponseEntity.ok(heldInvoiceRepository.findByStatus("HELD"));
    }

    /**
     * Retrieves a specific held invoice to resume the transaction.
     * Maps to: GET /pos/resume/{id}
     */
    @GetMapping("/resume/{id}")
    public ResponseEntity<HeldInvoice> resumeInvoice(@PathVariable Long id) {
        // Use idiomatic Optional mapping
        return heldInvoiceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a held invoice record by ID.
     * Maps to: DELETE /pos/held/delete/{id}
     */
    @DeleteMapping("/held/delete/{id}")
    public ResponseEntity<String> deleteHeldInvoice(@PathVariable Long id) {
        heldInvoiceRepository.deleteById(id);
        log.info("Held invoice deleted successfully: {}", id);
        return ResponseEntity.ok("Held invoice deleted successfully.");
    }

    // ----------------------------------------------------------------------
    //                           PAYMENT ENDPOINTS
    // ----------------------------------------------------------------------

    /**
     * Processes a payment for an existing SalesOrder. This handles both new payments
     * and partial payments for an existing order.
     * Maps to: POST /pos/receivePayment
     */
    @PostMapping("/receivePayment")
    public ResponseEntity<String> receivePayment(
            @RequestParam("invoiceNumber") String invoiceNumber,
            @RequestParam("amountPaid") BigDecimal amountPaid,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("finAccName") String finAccName) {
        
        try {
            Optional<SalesOrder> orderOpt = orderRepository.findByInvoiceNumber(invoiceNumber);
            
            if (orderOpt.isEmpty()) {
                log.warn("Payment received for non-existent order: {}", invoiceNumber);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"Order not found\",\"message\":\"No SalesOrder found for invoice: " + invoiceNumber + "\"}");
            } 
            
            SalesOrder order = orderOpt.get();
            
            // 1. Check for existing payment record (for partial payments)
            Payment payment = paymentRepository.findByTrnRefNo(invoiceNumber);
            
            if (payment == null) {
                // --- New Payment Record ---
                payment = new Payment();
                payment.setTrnRefNo(invoiceNumber);
                payment.setRefId(order.getId());
                payment.setRefType(RefType.SALE_ORDER);
                payment.setGrandTotal(order.getGrandTotal());
                payment.setAmountPaid(amountPaid);
                payment.setAmountDue(order.getGrandTotal().subtract(amountPaid));
                payment.setMethod(paymentMethod);
                payment.setFromAccount(finAccName);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setPaymentType("Revenue");
            } else {
                // --- Update Existing Payment (Partial Payment) ---
                BigDecimal newPaid = payment.getAmountPaid().add(amountPaid);
                payment.setAmountPaid(newPaid);
                
                // Calculate new due amount, ensuring it doesn't go below zero
                BigDecimal newDue = payment.getGrandTotal().subtract(newPaid).max(BigDecimal.ZERO);
                payment.setAmountDue(newDue);
                
                // Update method/account/date to reflect the latest transaction
                payment.setMethod(paymentMethod);
                payment.setFromAccount(finAccName);
                payment.setPaymentDate(LocalDateTime.now());
            }

            // 2. Update status and save
            // NOTE: The decompiled code relies on a method `payment.updatePaymentStatus()`. 
            // We assume this method exists and correctly sets the status (PAID/PARTIALLY_PAID/DUE).
            payment.updatePaymentStatus(payment.getAmountPaid());
            paymentRepository.save(payment);
            
            log.info("Payment received for invoice {}. New amount paid: {}", invoiceNumber, payment.getAmountPaid());
            
            // Return the created/updated payment object as JSON (converted automatically by Spring)
            return ResponseEntity.ok(payment.toString());
            
        } catch (Exception e) {
            log.error("Failed to receive payment for invoice {}.", invoiceNumber, e);
            // Return JSON-formatted error body
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal server error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}