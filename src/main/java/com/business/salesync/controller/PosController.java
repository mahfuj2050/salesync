package com.business.salesync.controller;

// --- Standard Java Imports (Organized) ---
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;

// --- Spring Framework Imports ---
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; // Added for cleaner URL mapping
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// --- Third-Party/Utility Imports ---
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor; // Added for dependency injection
import lombok.extern.slf4j.Slf4j; // Added for proper logging

// --- Application Model and Service Imports ---
import com.business.salesync.models.Customer;
import com.business.salesync.models.OrderDetails;
import com.business.salesync.models.Product;
import com.business.salesync.models.SalesOrder;
import com.business.salesync.models.Payment.RefType;
import com.business.salesync.models.PurchaseOrder.PaymentStatus;
import com.business.salesync.repository.CategoryRepository;
import com.business.salesync.repository.CustomerRepository;
import com.business.salesync.repository.FinancialAccountRepository;
import com.business.salesync.repository.HeldInvoiceRepository;
import com.business.salesync.repository.OrderRepository;
import com.business.salesync.repository.PaymentRepository;
import com.business.salesync.repository.ProductRepository;
import com.business.salesync.service.FinancialAccountService;

@Slf4j // Use SLF4J for proper logging instead of System.err
@Controller
@RequestMapping("/pos") // Set base URL mapping here
@RequiredArgsConstructor // Inject final fields via constructor (replaces verbose @Autowired on every field)
public class PosController {

    // --- Repositories and Services (Injected via Constructor) ---
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final PaymentController paymentController;
    private final FinancialAccountService financialAccountService;
    private final HeldInvoiceRepository heldInvoiceRepository;

    // --- Public Controller Methods ---

    /**
     * Displays the Point of Sale (POS) page with necessary data.
     */
    @GetMapping
    public String showPosPage(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("customer", new Customer());
        return "fragments/pos";
    }

    /**
     * Processes the checkout form submission, creating a Sales Order and associated records.
     */
    @PostMapping("/checkout") // Resolves to /pos/checkout
    public String checkout(@RequestParam Map<String, String> formData, RedirectAttributes redirect) {
        try {
            // --- 1. Parse and Validate Financials ---
            
            // Use the utility method to ensure a unique number
            String invoiceNumber = this.generateUniqueInvoiceNumber(formData.get("invoiceNumber")); 
            
            // Use utility methods (without 'this.') for clarity
            BigDecimal subTotal = parseBigDecimalSafe(formData.get("subTotal"));
            BigDecimal discount = parseBigDecimalSafe(formData.get("discount"));
            BigDecimal grandTotal = parseBigDecimalSafe(formData.get("grandTotal"));
            BigDecimal amountPaid = parseBigDecimalSafe(formData.get("amountPaid"));
            BigDecimal amountDue = parseBigDecimalSafe(formData.get("amountDue"));

            if (grandTotal == null || amountPaid == null) {
                redirect.addFlashAttribute("error", "Grand total and amount paid are required.");
                return "redirect:/pos";
            }

            // --- 2. Customer and Order Initialization ---

            Long customerId = Optional.ofNullable(formData.get("customerId"))
                .filter(s -> !s.isEmpty()).map(Long::parseLong).orElse(null);

            Customer customer = Optional.ofNullable(customerId)
                .flatMap(customerRepository::findById)
                .orElse(customerRepository.findById(1L).orElse(null)); // Walk-in fallback

            SalesOrder order = new SalesOrder();
            order.setInvoiceNumber(invoiceNumber);
            order.setTotalAmount(subTotal);
            order.setDiscount(discount != null ? discount : BigDecimal.ZERO);
            order.setGrandTotal(grandTotal);
            order.setAmountPaid(amountPaid);
            order.setAmountDue(amountDue != null ? amountDue : BigDecimal.ZERO);

            String dateOrderedStr = formData.get("dateOrdered");
            LocalDate dateOrdered = Optional.ofNullable(dateOrderedStr)
                    .filter(s -> !s.isEmpty()).map(LocalDate::parse).orElse(LocalDate.now());
            order.setDateOrdered(dateOrdered);
            
            order.setCustomer(customer);
            order.setWarrantyPeriod(parseIntegerSafe(formData.get("warrantyPeriod")));
            order.setWarrantyDescription(formData.get("warrantyDescription"));
            order.setGuaranteePeriod(parseIntegerSafe(formData.get("guaranteePeriod")));
            order.setGuaranteeDescription(formData.get("guaranteeDescription"));

            // --- 3. Process Order Details and Update Stock ---

            List<OrderDetails> details = new ArrayList<>();
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
                            if (currentStock < quantity) {
                                throw new RuntimeException("Insufficient stock for " + product.getName());
                            }

                            // Update stock and save product immediately
                            product.setQuantity(currentStock - quantity);
                            productRepository.save(product);

                            // Create OrderDetail
                            OrderDetails od = new OrderDetails();
                            od.setOrder(order);
                            od.setProduct(product);
                            od.setQuantity(quantity);
                            od.setUnitPrice(price);
                            od.setInvoiceNumber(invoiceNumber);
                            details.add(od);
                        }
                    } catch (Exception ex) {
                        // Use proper logging
                        log.error("Error processing product: {} - {}", e.getKey(), ex.getMessage());
                        // The original code uses System.err, which is replaced by log.error here. 
                        // It then continues processing the stream, but if it throws a RuntimeException,
                        // the stream stops. Keeping the logic flow consistent.
                    }
                });

            if (details.isEmpty()) {
                redirect.addFlashAttribute("error", "No valid products in order.");
                return "redirect:/pos";
            }
            
            // --- 4. Finalize Order and Save ---
            order.setOrderDetails(details);
            orderRepository.save(order);

            // --- 5. Payment Record and Financial Transaction ---

            String entityName = customer != null ? customer.getName() : "Walk-in Customer";
            
            if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                // Save Payment Record
                paymentController.savePaymentRecord(
                    order.getId(), RefType.SALE_ORDER, order.getTotalAmount(), 
                    order.getTotalVat(), order.getDiscount(), order.getGrandTotal(), 
                    order.getAmountPaid(), order.getAmountDue(), order.getInvoiceNumber(), entityName
                );

                // Record Financial Transaction
                String paymentMethod = formData.get("paymentMethod");
                String finAccName = switch (paymentMethod) { // Use Java 17+ switch expression
                    case "Cash" -> "Cash at Hand";
                    case "bKash" -> "bKash";
                    case "Bank Transfer" -> "Pubali Bank PLC";
                    default -> "Cash at Hand";
                };

                PaymentStatus poPaymentStatus = (amountPaid.compareTo(grandTotal) < 0)
                        ? PaymentStatus.PARTIALLY_PAID
                        : PaymentStatus.PAID;

                financialAccountService.recordTransaction(
                    finAccName, paymentMethod, amountPaid.doubleValue(), 
                    "CASH_IN", "SALE_ORDER", "CUSTOMER", 
                    entityName, invoiceNumber, order.getId(), 
                    "POS Sale Payment", poPaymentStatus.name()
                );
            }

            // --- 6. Redirect to Invoice ---
            redirect.addFlashAttribute("success", "Order placed successfully. Invoice: " + invoiceNumber);
            return "redirect:/pos/invoice/" + order.getId();

        } catch (Exception e) {
            // Use proper logging for the entire transaction failure
            log.error("Checkout failed.", e);
            redirect.addFlashAttribute("error", "Checkout failed: " + e.getMessage());
            return "redirect:/pos";
        }
    }

    /**
     * Displays the invoice page for a specific Sales Order, including a QR code.
     */
    @GetMapping("/invoice/{id}") // Resolves to /pos/invoice/{id}
    public String showInvoiceQr(@PathVariable Long id, Model model) throws Exception {
        SalesOrder order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        
        String invoiceNumber = order.getInvoiceNumber();
        String url = "http://localhost:8080/pos/invoice/" + order.getId() + "?inv=" + invoiceNumber;
        
        // Generate QR Code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(
            qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200)
        );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        String qrCodeBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        
        // Calculate display values
        BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
        BigDecimal grandTotal = order.getGrandTotal() != null ? order.getGrandTotal() : BigDecimal.ZERO;
        BigDecimal totalBeforeDiscount = grandTotal.add(discount);

        // Add attributes to model
        model.addAttribute("order", order);
        model.addAttribute("invoiceNumber", invoiceNumber);
        model.addAttribute("qrCode", qrCodeBase64);
        model.addAttribute("qrUrl", url);
        model.addAttribute("discount", discount);
        model.addAttribute("totalBeforeDiscount", totalBeforeDiscount);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("amountPaid", order.getAmountPaid() != null ? order.getAmountPaid() : BigDecimal.ZERO);
        model.addAttribute("amountDue", order.getAmountDue() != null ? order.getAmountDue() : BigDecimal.ZERO);
        model.addAttribute("warrantyPeriod", order.getWarrantyPeriod());
        model.addAttribute("warrantyDescription", order.getWarrantyDescription());
        model.addAttribute("guaranteePeriod", order.getGuaranteePeriod());
        model.addAttribute("guaranteeDescription", order.getGuaranteeDescription());
        
        return "fragments/invoice";
    }
    
    // --- Private Utility Methods ---
    
    /**
     * Safely converts a String value to BigDecimal, returning null on error or empty string.
     */
    private BigDecimal parseBigDecimalSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid BigDecimal: {}", value);
            return null;
        }
    }

    /**
     * Safely converts a String value to Integer, returning null on error or empty string.
     */
    private Integer parseIntegerSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value: {}", value);
            return null;
        }
    }

    /**
     * Generates a unique invoice number by checking existence in the repository.
     */
    private String generateUniqueInvoiceNumber(String baseInvoiceNumber) {
        String candidate = baseInvoiceNumber;

        for(int counter = 1; orderRepository.findByInvoiceNumber(candidate).isPresent(); ++counter) {
            if (baseInvoiceNumber.matches("\\d+")) {
                // If base is purely numeric, increment the number
                try {
                    candidate = String.valueOf(Integer.parseInt(baseInvoiceNumber) + counter);
                } catch (NumberFormatException e) {
                     // Fallback in case baseInvoiceNumber is too large or has unexpected characters despite the regex match
                     candidate = baseInvoiceNumber + "-" + counter;
                }
            } else {
                // Otherwise, append a hyphen and counter
                candidate = baseInvoiceNumber + "-" + counter;
            }
        }

        return candidate;
    }
}