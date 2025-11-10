package com.business.salesync.service;

import com.business.salesync.models.Payment;
import com.business.salesync.models.SalesOrder;
import com.business.salesync.models.Payment.RefType;
import com.business.salesync.repository.OrderRepository;
import com.business.salesync.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import lombok.Generated;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
   private final PaymentRepository paymentRepository;
   private final OrderRepository salesOrderRepository;

   @Transactional
   public Payment savePayment(Payment payment, String invoiceNumber) {
      SalesOrder order = (SalesOrder)this.salesOrderRepository.findByInvoiceNumber(invoiceNumber).orElseThrow(() -> {
         return new IllegalArgumentException("Invalid Invoice Number: " + invoiceNumber);
      });
      payment.setRefId(order.getId());
      payment.setTrnRefNo(invoiceNumber);
      payment.setRefType(RefType.SALE_ORDER);
      Payment savedPayment = (Payment)this.paymentRepository.save(payment);
      this.updateSalesOrderPayment(order, payment.getAmountPaid());
      return savedPayment;
   }

   private void updateSalesOrderPayment(SalesOrder order, BigDecimal newPayment) {
      if (newPayment != null) {
         BigDecimal updatedPaid = order.getAmountPaid().add(newPayment);
         order.setAmountPaid(updatedPaid);
         BigDecimal newDue = order.getGrandTotal().subtract(updatedPaid);
         order.setAmountDue(newDue.max(BigDecimal.ZERO));
         order.updatePaymentStatus();
         this.salesOrderRepository.save(order);
      }
   }

   @Generated
   public PaymentService(final PaymentRepository paymentRepository, final OrderRepository salesOrderRepository) {
      this.paymentRepository = paymentRepository;
      this.salesOrderRepository = salesOrderRepository;
   }
}