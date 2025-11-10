package com.business.salesync.controller;

import com.business.salesync.models.Payment;
import com.business.salesync.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentRestController {

    private final PaymentService paymentService;

    @PostMapping("/save")
    public ResponseEntity<?> savePayment(@RequestBody Payment payment) {
        try {
            Payment savedPayment = paymentService.savePayment(payment, payment.getTrnRefNo());
            return ResponseEntity.ok(savedPayment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process payment."));
        }
    }
}
