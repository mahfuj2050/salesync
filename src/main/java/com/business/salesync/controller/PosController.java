package com.business.salesync.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.salesync.models.Customer;
import com.business.salesync.models.FinancialAccount;
import com.business.salesync.models.OrderDetails;
import com.business.salesync.models.Payment;
import com.business.salesync.models.Product;
import com.business.salesync.models.SalesOrder;
import com.business.salesync.models.PurchaseOrder.PaymentStatus;
import com.business.salesync.repository.CategoryRepository;
import com.business.salesync.repository.CustomerRepository;
import com.business.salesync.repository.FinancialAccountRepository;
import com.business.salesync.repository.OrderRepository;
import com.business.salesync.repository.PaymentRepository;
import com.business.salesync.repository.ProductRepository;
import com.business.salesync.service.FinancialAccountService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;



@Controller
public class PosController {
	
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FinancialAccountRepository financialAccountRepository;
    

    @Autowired
    private PaymentController paymentController;
    
    @Autowired
    private FinancialAccountService financialAccountService;
    
    

    @GetMapping("/pos")
    public String showPosPage(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("products", productRepository.findAll());

        // Add this line:
        model.addAttribute("customer", new Customer());

        return "fragments/pos";
    }


	/*
    @PostMapping("/pos/checkout")
    public String checkout(@RequestParam Map<String, String> formData, RedirectAttributes redirect) {
        // Process order & payment here
        redirect.addFlashAttribute("success", "Order completed successfully!");
        return "redirect:/pos";
    }*/
    
	/*-
    @PostMapping("/pos/checkout")
    public String checkout(@RequestParam Map<String, String> formData, RedirectAttributes redirect) {
    	
    	// ✅ Debug: Print all form data
        System.out.println("=== FORM DATA RECEIVED ===");
        formData.forEach((key, value) -> {
            System.out.println(key + " = " + value);
        });
        System.out.println("==========================");
    	
        try {
            String invoiceNumber = formData.get("invoiceNumber");
            
            // ✅ Safe BigDecimal parsing with null checks
            BigDecimal subTotal = parseBigDecimalSafe(formData.get("subTotal"));
            BigDecimal discount = parseBigDecimalSafe(formData.get("discount"));
            BigDecimal grandTotal = parseBigDecimalSafe(formData.get("grandTotal"));
            BigDecimal amountPaid = parseBigDecimalSafe(formData.get("amountPaid"));
            BigDecimal amountDue = parseBigDecimalSafe(formData.get("amountDue"));

            // ✅ Validate required fields
            if (grandTotal == null || amountPaid == null) {
                redirect.addFlashAttribute("error", "Grand total and amount paid are required");
                return "redirect:/pos";
            }

            Long customerId = formData.get("customerId") != null && !formData.get("customerId").isEmpty()
                    ? Long.parseLong(formData.get("customerId")) : null;

            Customer customer = (customerId != null)
                    ? customerRepository.findById(customerId).orElse(null)
                    : customerRepository.findById(1L).orElse(null); // ✅ fallback Walk-in

            SalesOrder order = new SalesOrder();
            order.setInvoiceNumber(invoiceNumber);
            order.setTotalAmount(subTotal);
            order.setDiscount(discount != null ? discount : BigDecimal.ZERO);
            order.setGrandTotal(grandTotal);
            order.setAmountPaid(amountPaid);
            order.setAmountDue(amountDue != null ? amountDue : BigDecimal.ZERO);
            order.setDateOrdered(LocalDate.now());
            order.setCustomer(customer);

            List<OrderDetails> details = new ArrayList<>();

            formData.entrySet().stream()
                .filter(e -> e.getKey().startsWith("product_"))
                .forEach(e -> {
                    try {
                        Long productId = Long.parseLong(e.getKey().split("_")[1]);
                        
                        // ✅ Safe parsing for quantity and price
                        String quantityStr = formData.get("quantity_" + productId);
                        String priceStr = formData.get("sellingPrice_" + productId);
                        
                        if (quantityStr == null || priceStr == null) {
                            System.err.println("Missing quantity or price for product: " + productId);
                            return;
                        }
                        
                        int quantity = Integer.parseInt(quantityStr);
                        BigDecimal price = parseBigDecimalSafe(priceStr);
                        
                        if (price == null) {
                            System.err.println("Invalid price for product: " + productId);
                            return;
                        }

                        Product product = productRepository.findById(productId).orElse(null);
                        if (product != null) {
                            // ✅ Decrease stock
                            int currentStock = product.getQuantity();
                            if (currentStock < quantity) {
                                throw new RuntimeException("Insufficient stock for product: " + product.getName());
                            }
                            product.setQuantity(currentStock - quantity);
                            productRepository.save(product);

                            OrderDetails od = new OrderDetails();
                            od.setOrder(order);
                            od.setProduct(product);
                            od.setQuantity(quantity);
                            od.setUnitPrice(price);
                            od.setInvoiceNumber(invoiceNumber);
                            details.add(od);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error processing product: " + e.getKey() + " - " + ex.getMessage());
                        // Continue with other products instead of failing entire order
                    }
                });

            // ✅ Check if we have any valid order details
            if (details.isEmpty()) {
                redirect.addFlashAttribute("error", "No valid products in order");
                return "redirect:/pos";
            }

            order.setOrderDetails(details);

            // ✅ Debug logs
            System.out.println("📦 ORDER DATA:");
            System.out.println("Invoice: " + invoiceNumber);
            System.out.println("Total: " + grandTotal);
            System.out.println("Paid: " + amountPaid);
            System.out.println("Due: " + order.getAmountDue());
            System.out.println("Customer: " + (customer != null ? customer.getName() : "NULL"));
            System.out.println("Items count: " + details.size());

            details.forEach(d -> {
                System.out.println(" - Product ID: " + d.getProduct().getId() + 
                                   ", Qty: " + d.getQuantity() + 
                                   ", Price: " + d.getUnitPrice());
            });

            // ✅ Save order and cascade details
            orderRepository.save(order);

         // ✅ Create Payment record in Java instead of trigger
         // ✅ Create Payment record in Java instead of trigger
            Payment payment = new Payment();

            // Core info
            payment.setFromAccount("POS"); // optional, or set based on cashier/account source
            payment.setToAccount(order.getCustomer().getName());
            payment.setMethod("Cash");
            payment.setInstrumentNo(null); // optional if not used

            // Financials — fill all non-nullable fields
            payment.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
            payment.setTotalVat(order.getTotalVat() != null ? order.getTotalVat() : BigDecimal.ZERO);
            payment.setDiscount(order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO);
            payment.setGrandTotal(order.getGrandTotal() != null ? order.getGrandTotal() : BigDecimal.ZERO);
            payment.setAmountPaid(order.getAmountPaid() != null ? order.getAmountPaid() : BigDecimal.ZERO);
            payment.setAmountDue(order.getAmountDue() != null ? order.getAmountDue() : BigDecimal.ZERO);

            // ✅ Ensure paidAmount (new field) is also populated
            payment.setPaidAmount(order.getAmountPaid() != null ? order.getAmountPaid() : BigDecimal.ZERO);

            // Date and reference
            payment.setPaymentDate(LocalDateTime.now());
            payment.setRefId(order.getId());
            payment.setRefType(Payment.RefType.SALE_ORDER);
            payment.setRemarks("Auto payment for Order #" + order.getInvoiceNumber());

            // ✅ Determine payment status properly
            payment.updatePaymentStatus(payment.getAmountPaid());

            // Or explicitly set from order
            payment.setPaymentStatus(Payment.PaymentStatus.valueOf(order.getPaymentStatus().name()));

            // Save
            paymentRepository.save(payment);



            redirect.addFlashAttribute("success", "Order placed successfully. Invoice: " + invoiceNumber);
            return "redirect:/pos/invoice/" + order.getId();
            
        } catch (Exception e) {
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Checkout failed: " + e.getMessage());
            return "redirect:/pos";
        }
    }
    
    */
    
    @PostMapping("/pos/checkout")
    public String checkout(@RequestParam Map<String, String> formData, RedirectAttributes redirect) {

        // ✅ Debug: Print all form data
        System.out.println("=== FORM DATA RECEIVED ===");
        formData.forEach((key, value) -> System.out.println(key + " = " + value));
        System.out.println("==========================");

        try {
            String invoiceNumber = formData.get("invoiceNumber");

            // ✅ Safe BigDecimal parsing
            BigDecimal subTotal = parseBigDecimalSafe(formData.get("subTotal"));
            BigDecimal discount = parseBigDecimalSafe(formData.get("discount"));
            BigDecimal grandTotal = parseBigDecimalSafe(formData.get("grandTotal"));
            BigDecimal amountPaid = parseBigDecimalSafe(formData.get("amountPaid"));
            BigDecimal amountDue = parseBigDecimalSafe(formData.get("amountDue"));

            // ✅ Validate required fields
            if (grandTotal == null || amountPaid == null) {
                redirect.addFlashAttribute("error", "Grand total and amount paid are required");
                return "redirect:/pos";
            }

            Long customerId = formData.get("customerId") != null && !formData.get("customerId").isEmpty()
                    ? Long.parseLong(formData.get("customerId")) : null;

            Customer customer = (customerId != null)
                    ? customerRepository.findById(customerId).orElse(null)
                    : customerRepository.findById(1L).orElse(null); // fallback Walk-in

            SalesOrder order = new SalesOrder();
            order.setInvoiceNumber(invoiceNumber);
            order.setTotalAmount(subTotal);
            order.setDiscount(discount != null ? discount : BigDecimal.ZERO);
            order.setGrandTotal(grandTotal);
            order.setAmountPaid(amountPaid);
            order.setAmountDue(amountDue != null ? amountDue : BigDecimal.ZERO);
            order.setDateOrdered(LocalDate.now());
            order.setCustomer(customer);

            List<OrderDetails> details = new ArrayList<>();

            // ✅ Process products
            formData.entrySet().stream()
                .filter(e -> e.getKey().startsWith("product_"))
                .forEach(e -> {
                    try {
                        Long productId = Long.parseLong(e.getKey().split("_")[1]);
                        String quantityStr = formData.get("quantity_" + productId);
                        String priceStr = formData.get("sellingPrice_" + productId);
                        if (quantityStr == null || priceStr == null) return;

                        int quantity = Integer.parseInt(quantityStr);
                        BigDecimal price = parseBigDecimalSafe(priceStr);
                        if (price == null) return;

                        Product product = productRepository.findById(productId).orElse(null);
                        if (product != null) {
                            int currentStock = product.getQuantity();
                            if (currentStock < quantity) throw new RuntimeException("Insufficient stock for " + product.getName());
                            product.setQuantity(currentStock - quantity);
                            productRepository.save(product);

                            OrderDetails od = new OrderDetails();
                            od.setOrder(order);
                            od.setProduct(product);
                            od.setQuantity(quantity);
                            od.setUnitPrice(price);
                            od.setInvoiceNumber(invoiceNumber);
                            details.add(od);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error processing product: " + e.getKey() + " - " + ex.getMessage());
                    }
                });

            if (details.isEmpty()) {
                redirect.addFlashAttribute("error", "No valid products in order");
                return "redirect:/pos";
            }

            order.setOrderDetails(details);
            orderRepository.save(order);

            // ✅ Save Payment using PaymentController helper
            if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                String entityName = (customer != null) ? customer.getName() : "Walk-in Customer";
                paymentController.savePaymentRecord(
                        order.getId(),
                        Payment.RefType.SALE_ORDER,
                        order.getTotalAmount(),
                        order.getTotalVat(),
                        order.getDiscount(),
                        order.getGrandTotal(),
                        order.getAmountPaid(),
                        order.getAmountDue(),
                        entityName
                );
            }
            
            // -------------------------------
            // 1️⃣ Record financial transaction
            // -------------------------------
            if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                String entityName = (customer != null) ? customer.getName() : "Walk-in Customer";

                // Decide account name based on payment method
                String paymentMethod = formData.get("paymentMethod"); // "Cash", "bKash", "Bank Transfer"
                String finAccName = switch (paymentMethod) {
                    case "Cash" -> "Cash at Hand";
                    case "bKash" -> "bKash";
                    case "Bank Transfer" -> "Pubali Bank PLC"; // choose appropriate bank
                    default -> "Cash at Hand";
                };

                String transactionType = "CASH_IN"; // POS collection is money coming in
                
                PaymentStatus poPaymentStatus;
                if (amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
                    poPaymentStatus = PaymentStatus.PENDING;
                } else if (amountPaid.compareTo(grandTotal) < 0) {
                    poPaymentStatus = PaymentStatus.PARTIALLY_PAID;
                } else {
                    poPaymentStatus = PaymentStatus.PAID;
                }


                financialAccountService.recordTransaction(
                        finAccName,
                        paymentMethod,        // finAccType
                        amountPaid.doubleValue(),
                        transactionType,      // CASH_IN
                        "SALE_ORDER",         // refType
                        "CUSTOMER",           // entityType
                        entityName,
                        invoiceNumber,        // trnRefNo
                        order.getId(),        // refId
                        "POS Sale Payment",   // remarks
                        poPaymentStatus.name()         // paymentStatus as String
                );
            }

            redirect.addFlashAttribute("success", "Order placed successfully. Invoice: " + invoiceNumber);
            return "redirect:/pos/invoice/" + order.getId();

        } catch (Exception e) {
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Checkout failed: " + e.getMessage());
            return "redirect:/pos";
        }
    }


    // ✅ Helper method for safe BigDecimal parsing
    private BigDecimal parseBigDecimalSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid BigDecimal value: " + value);
            return null;
        }
    }
    
	/*-
    @GetMapping("/pos/invoice/{id}")
    public String showInvoice(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));

        model.addAttribute("order", order);
        model.addAttribute("invoiceNumber", order.getInvoiceNumber()); // pass invoiceNumber
        return "fragments/invoice"; // invoice.html
    }*/

    @GetMapping("/pos/invoice/{id}")
    public String showInvoiceQr(@PathVariable Long id, Model model) throws Exception {
        SalesOrder order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));

        String invoiceNumber = order.getInvoiceNumber();

        // Create full URL to encode in QR code
        String url = "http://localhost:8080/pos/invoice/" + order.getId() + "?inv=" + invoiceNumber;

        // Generate QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(
            qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200)
        );

        // Convert QR image to Base64 for embedding in HTML
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(qrImage, "PNG", baos);
        String qrCodeBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

        // ✅ Compute display values
        BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
        BigDecimal grandTotal = order.getGrandTotal() != null ? order.getGrandTotal() : BigDecimal.ZERO;
        BigDecimal amountPaid = order.getAmountPaid() != null ? order.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal amountDue = order.getAmountDue() != null ? order.getAmountDue() : BigDecimal.ZERO;
        BigDecimal totalBeforeDiscount = grandTotal.add(discount); // reverse-engineered total

        // ✅ Add all data to model
        model.addAttribute("order", order);
        model.addAttribute("invoiceNumber", invoiceNumber);
        model.addAttribute("qrCode", qrCodeBase64);
        model.addAttribute("qrUrl", url);

        // Financial summary
        model.addAttribute("discount", discount);
        model.addAttribute("totalBeforeDiscount", totalBeforeDiscount);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("amountPaid", amountPaid);
        model.addAttribute("amountDue", amountDue);

        return "fragments/invoice"; // invoice.html
    }

    
    @PostMapping("/pos/receivePayment")
    @ResponseBody
    public ResponseEntity<?> receivePayment(
            @RequestParam Long orderId,
            @RequestParam BigDecimal amountPaid,
            @RequestParam(required = false) String paymentMethod, // CASH, MFS, BANK
            @RequestParam(required = false) String finAccName      // e.g. "Cash at Hand", "bKash-01717...", etc.
    ) {
        Optional<SalesOrder> optionalOrder = orderRepository.findById(orderId);
        if (!optionalOrder.isPresent()) {
            return ResponseEntity.badRequest().body("Order not found");
        }

        SalesOrder order = optionalOrder.get();

        // --- 1️⃣ Update SalesOrder ---
        BigDecimal newAmountPaid = order.getAmountPaid().add(amountPaid);
        BigDecimal newAmountDue = order.getGrandTotal().subtract(newAmountPaid);

        order.setAmountPaid(newAmountPaid);
        order.setAmountDue(newAmountDue);
        order.setPaymentStatus(calculatePaymentStatus(newAmountPaid, order.getGrandTotal()));
        orderRepository.save(order);

        // --- 2️⃣ Create Payment Record ---
        Payment payment = new Payment();
        payment.setRefId(order.getId());
        payment.setRefType(Payment.RefType.SALE_ORDER);
        payment.setEntityType("Customer");
        payment.setCustomer(order.getCustomer());
        payment.setPaidAmount(amountPaid);
        payment.setAmountPaid(newAmountPaid);
        payment.setAmountDue(newAmountDue);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(Payment.PaymentStatus.PAID); // or PARTIALLY_PAID
        payment.setMethod(paymentMethod);
        payment.setFromAccount(finAccName);  // ✅ now storing the real account name
        payment.setGrandTotal(order.getGrandTotal());
        payment.setTotalAmount(order.getTotalAmount());
        payment.setDiscount(order.getDiscount());
        payment.setTotalVat(order.getTotalVat());
        paymentRepository.save(payment);

        // --- 3️⃣ Update FinancialAccount ---
        updateFinancialAccountForPayment(order, payment);

        // --- Response ---
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("amountPaid", newAmountPaid);
        response.put("amountDue", newAmountDue);

        return ResponseEntity.ok(response);
    }

    // Helper method to determine payment status
    private SalesOrder.PaymentStatus calculatePaymentStatus(BigDecimal amountPaid, BigDecimal grandTotal) {
        if (amountPaid.compareTo(grandTotal) >= 0) {
            return SalesOrder.PaymentStatus.PAID;
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            return SalesOrder.PaymentStatus.PARTIALLY_PAID;
        } else {
            return SalesOrder.PaymentStatus.PENDING;
        }
    }

    // Helper to update FinancialAccount
    private void updateFinancialAccountForPayment(SalesOrder order, Payment payment) {
        String accountName = payment.getFromAccount(); // e.g. "Cash at Hand"

        // Find matching account (master)
        List<FinancialAccount> accounts = financialAccountRepository.findByFinAccNameIgnoreCase(accountName);
        if (accounts.isEmpty()) {
            throw new RuntimeException("Financial account not found: " + accountName);
        }

        FinancialAccount account = accounts.get(0);

        // Create new transaction row (ledger entry)
        FinancialAccount trn = new FinancialAccount();
        trn.setFinAccId(account.getFinAccId());
        trn.setFinAccName(account.getFinAccName());
        trn.setFinAccType(account.getFinAccType());
        trn.setTrnDate(LocalDateTime.now());
        trn.setRefId(order.getId());
        trn.setTrnRefNo(order.getInvoiceNumber()); // ✅ Save the actual invoice number
        trn.setRefType("SALE_ORDER");
        trn.setTransactionType("CASH_IN");
        trn.setEntityName(order.getCustomer().getName());
        trn.setEntityType("CUSTOMER");
        trn.setPaymentMethod(payment.getMethod());
        trn.setPaymentStatus(payment.getPaymentStatus().name());
        trn.setCreditAmount(payment.getPaidAmount().doubleValue());
        trn.setCurrentBalance(account.getCurrentBalance() + payment.getPaidAmount().doubleValue());
        trn.setBalanceAfterTransaction(account.getCurrentBalance() + payment.getPaidAmount().doubleValue());
        trn.setRemarks("Payment received for invoice " + order.getInvoiceNumber());

        // Update main account balance
        account.setCurrentBalance(account.getCurrentBalance() + payment.getPaidAmount().doubleValue());
        financialAccountRepository.save(account);
        financialAccountRepository.save(trn);
    }




}
