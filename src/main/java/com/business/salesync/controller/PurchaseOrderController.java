package com.business.salesync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.business.salesync.dto.PurchaseOrderDTO;
import com.business.salesync.dto.PurchaseOrderItemDTO;
import com.business.salesync.models.Product;
import com.business.salesync.models.PurchaseOrder;
import com.business.salesync.models.PurchaseOrderItem;
import com.business.salesync.models.Supplier;
import com.business.salesync.repository.ProductRepository;
import com.business.salesync.repository.PurchaseOrderItemRepository;
import com.business.salesync.repository.PurchaseOrderRepository;
import com.business.salesync.repository.SupplierRepository;
import com.business.salesync.service.PONumberService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderRepository purchaseOrderRepo;
    private final PurchaseOrderItemRepository purchaseOrderItemRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;
    
    @Autowired
    private PONumberService poNumberService;

    // =======================
    // 1️⃣ List all purchase orders
    // =======================
    @GetMapping
    public String listPurchaseOrders(Model model) {
        List<PurchaseOrderDTO> orders = purchaseOrderRepo.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        model.addAttribute("orders", orders);
        return "fragments/purchases";
    }

    // =======================
    // 2️⃣ Show new purchase order form
    // =======================
    @GetMapping("/new")
    public String showPurchaseForm(Model model) {
        PurchaseOrder po = new PurchaseOrder();

        // Generate PO number
        PurchaseOrder lastPo = purchaseOrderRepo.findTopByOrderByInsertDateDesc();
        String poNo = "PO-" + LocalDateTime.now().toLocalDate().toString().replace("-", "") + "-001";
        if (lastPo != null && lastPo.getPurchaseOrderNo() != null) {
            poNo = incrementPONumber(lastPo.getPurchaseOrderNo());
        }
        po.setPurchaseOrderNo(poNo);

        // Get data
        List<Supplier> suppliers = supplierRepo.findAll();
        List<Product> products = productRepo.findAll();

        model.addAttribute("purchaseOrder", po);
        model.addAttribute("suppliers", suppliers != null ? suppliers : Collections.emptyList());
        model.addAttribute("products", products != null ? products : Collections.emptyList());
        
        // Create a simplified DTO for products to avoid circular references
        List<Map<String, Object>> simpleProducts = new ArrayList<>();
        if (products != null) {
            for (Product product : products) {
                Map<String, Object> simpleProduct = new HashMap<>();
                simpleProduct.put("id", product.getId());
                simpleProduct.put("name", product.getName());
                simpleProduct.put("quantity", product.getQuantity());
                simpleProduct.put("costPrice", product.getCostPrice());
                simpleProduct.put("sellingPrice", product.getSellingPrice());
                simpleProduct.put("minStockLevel", product.getMinStockLevel());
                simpleProducts.add(simpleProduct);
            }
        }
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            String productsJson = mapper.writeValueAsString(simpleProducts);
            model.addAttribute("productsJson", productsJson);
        } catch (Exception e) {
            model.addAttribute("productsJson", "[]");
        }

        return "fragments/purchase_form";
    }

    private String incrementPONumber(String lastPoNo) {
        try {
            String[] parts = lastPoNo.split("-");
            int number = Integer.parseInt(parts[2]);
            number++;
            return parts[0] + "-" + parts[1] + "-" + String.format("%03d", number);
        } catch (Exception e) {
            return "PO-" + LocalDateTime.now().toLocalDate().toString().replace("-", "") + "-001";
        }
    }

    // =======================
    // 3️⃣ Save purchase order with VAT, discount, and stock update
    // =======================
    @PostMapping("/save")
    public String savePurchaseOrder(@ModelAttribute PurchaseOrder purchaseOrder,
                                    @RequestParam List<Long> productIds,
                                    @RequestParam List<Integer> quantities,
                                    @RequestParam List<Double> purchasePrices,
                                    @RequestParam List<Double> sellingPrices,
                                    @RequestParam(required = false) List<Double> vatPercents,
                                    @RequestParam(required = false, defaultValue = "0") double discount,
                                    @RequestParam(required = false, defaultValue = "0") double amountPaid,
                                    RedirectAttributes redirectAttributes) {

        // ✅ Validation
        if (productIds == null || productIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No products selected for purchase!");
            return "redirect:/purchase/new";
        }

        // Validate all required parameters
        if (quantities.size() != productIds.size() || purchasePrices.size() != productIds.size() || 
            sellingPrices.size() != productIds.size()) {
            redirectAttributes.addFlashAttribute("error", "Invalid product data submitted!");
            return "redirect:/purchase/new";
        }

        // Generate PO number
        String poNumber = poNumberService.generateNextPONumber();
        purchaseOrder.setPurchaseOrderNo(poNumber);
        purchaseOrder.setInsertDate(LocalDate.now());
        purchaseOrder.setUpdateDate(LocalDate.now());
        purchaseOrder.setStatus("RECEIVED");

        // ✅ Save PO first (without financial details)
        PurchaseOrder savedPo = purchaseOrderRepo.save(purchaseOrder);

        List<PurchaseOrderItem> items = new ArrayList<>();
        double totalAmount = 0;
        double totalVatAmount = 0;

        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            Integer qty = quantities.get(i);
            Double purchasePrice = purchasePrices.get(i);
            Double sellingPrice = sellingPrices.get(i);
            Double vatPercent = (vatPercents != null && vatPercents.size() > i) ? vatPercents.get(i) : 0.0;

            Product product = productRepo.findById(productId).orElse(null);
            if (product == null || qty <= 0) continue;

            // Validate prices
            if (purchasePrice <= 0 || sellingPrice <= 0) {
                redirectAttributes.addFlashAttribute("error", 
                    "Invalid prices for product: " + product.getName());
                return "redirect:/purchase/new";
            }

            double subtotal = purchasePrice * qty;
            double vatAmount = subtotal * vatPercent / 100.0;

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(savedPo)
                    .product(product)
                    .quantity(qty)
                    .purchasePrice(purchasePrice)
                    .sellingPrice(sellingPrice)
                    .subtotal(subtotal)
                    .vatPercent(vatPercent)
                    .vatAmount(vatAmount)
                    .build();

            items.add(item);
            totalAmount += subtotal;
            totalVatAmount += vatAmount;

            // ✅ Update product stock & pricing with WEIGHTED AVERAGE COST
            updateProductStockAndCost(product, qty, purchasePrice, sellingPrice);
        }

        // ✅ Save all purchase items
        purchaseOrderItemRepo.saveAll(items);

        // ✅ Final calculations
        double totalWithVat = totalAmount + totalVatAmount;
        double grandTotal = totalWithVat - discount;
        double amountDue = grandTotal - amountPaid;

        // Update the saved PO with financial details (DO NOT set items collection)
        savedPo.setTotalAmount(totalAmount);
        savedPo.setVatAmount(BigDecimal.valueOf(totalVatAmount));
        savedPo.setDiscount(discount);
        savedPo.setGrandTotal(grandTotal);
        savedPo.setAmountPaid(amountPaid);
        savedPo.setAmountDue(amountDue);
        savedPo.setRemarks("VAT: " + totalVatAmount);
        // REMOVE THIS LINE: savedPo.setItems(items); // This causes the orphan removal error
        savedPo.updatePaymentStatus(amountPaid);

        purchaseOrderRepo.save(savedPo);

        redirectAttributes.addFlashAttribute("success", 
            "Purchase order " + poNumber + " saved successfully!");
        return "redirect:/purchase";
    }

    /**
     * Update product stock and calculate weighted average cost
     */
    private void updateProductStockAndCost(Product product, int purchasedQty, 
                                         double purchasePrice, double sellingPrice) {
        int currentStock = product.getQuantity();
        BigDecimal currentCost = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
        
        // Calculate new stock
        int newStock = currentStock + purchasedQty;
        
        // Calculate weighted average cost
        BigDecimal newCost;
        if (newStock > 0) {
            BigDecimal currentTotalCost = currentCost.multiply(BigDecimal.valueOf(currentStock));
            BigDecimal newTotalCost = BigDecimal.valueOf(purchasePrice * purchasedQty);
            BigDecimal totalCost = currentTotalCost.add(newTotalCost);
            newCost = totalCost.divide(BigDecimal.valueOf(newStock), 2, RoundingMode.HALF_UP);
        } else {
            newCost = BigDecimal.valueOf(purchasePrice);
        }
        
        // Update product
        product.setQuantity(newStock);
        product.setCostPrice(newCost);
        product.setSellingPrice(BigDecimal.valueOf(sellingPrice));
        product.setUpdatedAt(LocalDateTime.now());
        
        productRepo.save(product);
    }

    // =======================
    // 4️⃣ View purchase order
    // =======================
    @GetMapping("/{id}")
    public String viewPurchaseOrder(@PathVariable Long id, Model model) {
        PurchaseOrder po = purchaseOrderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with id: " + id));
        model.addAttribute("purchaseOrder", mapToDTO(po));
        return "purchase/view";
    }

    // =======================
    // 5️⃣ Delete purchase order
    // =======================
    @PostMapping("/{id}/delete")
    public String deletePurchaseOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            PurchaseOrder po = purchaseOrderRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Purchase order not found"));
            
            // Check if there are any payments (you might want to add this check)
            // if (po.getPaymentStatus() == PaymentStatus.PAID || po.getPaymentStatus() == PaymentStatus.PARTIALLY_PAID) {
            //     redirectAttributes.addFlashAttribute("error", "Cannot delete purchase order with payments");
            //     return "redirect:/purchase";
            // }
            
            // Rollback product stock for all items
            for (PurchaseOrderItem item : po.getItems()) {
                rollbackProductStock(item.getProduct(), item.getQuantity());
            }
            
            purchaseOrderRepo.delete(po);
            redirectAttributes.addFlashAttribute("success", "Purchase order deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting purchase order: " + e.getMessage());
        }
        return "redirect:/purchase";
    }

    /**
     * Rollback product stock when deleting purchase order
     */
    private void rollbackProductStock(Product product, int purchasedQty) {
        int currentStock = product.getQuantity();
        int newStock = Math.max(0, currentStock - purchasedQty); // Prevent negative stock
        
        product.setQuantity(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        productRepo.save(product);
    }

    // =======================
    // ✅ DTO Mapper (null-safe)
    // =======================
    private PurchaseOrderDTO mapToDTO(PurchaseOrder po) {
        List<PurchaseOrderItem> poItems = po.getItems() == null ? Collections.emptyList() : po.getItems();

        List<PurchaseOrderItemDTO> itemDTOs = poItems.stream()
                .map(item -> PurchaseOrderItemDTO.builder()
                        .productName(item.getProduct() != null ? item.getProduct().getName() : "")
                        .quantity(item.getQuantity())
                        .purchasePrice(item.getPurchasePrice())
                        .sellingPrice(item.getSellingPrice())
                        .subtotal(item.getSubtotal())
                        .vatPercent(item.getVatPercent())
                        .vatAmount(item.getVatAmount())
                        .subtotalWithVat(item.getSubtotal() + item.getVatAmount())
                        .build())
                .collect(Collectors.toList());

        return PurchaseOrderDTO.builder()
                .id(po.getId())
                .purchaseOrderNo(po.getPurchaseOrderNo())
                .supplierName(po.getSupplier() != null ? po.getSupplier().getSupplierName() : "")
                .insertDate(po.getInsertDate())
                .updateDate(po.getUpdateDate())
                .totalAmount(po.getTotalAmount())
                .vatAmount(po.getVatAmount())
                .discount(po.getDiscount())
                .grandTotal(po.getGrandTotal())
                .amountPaid(po.getAmountPaid())
                .amountDue(po.getAmountDue())
                .status(po.getStatus())
                .paymentStatus(po.getPaymentStatus() != null ? po.getPaymentStatus().name() : "PENDING")
                .remarks(po.getRemarks())
                .items(itemDTOs)
                .build();
    }
}