package com.business.salesync.controller;

import com.business.salesync.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Handles delete operations for multiple entity types (Customer, Supplier, Product, etc.)
 * Performs dependency checks before deleting.
 */
@RestController
@RequestMapping("/api/delete")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DeleteController {

    private static final Logger log = LoggerFactory.getLogger(DeleteController.class);

    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final OrderRepository saleOrderRepository;
    private final OrderDetailsRepository saleOrderDetailsRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseItemRepository expenseItemRepository;
    private final FinancialAccountRepository financialAccountRepository;

    /**
     * Delete an entity by type and ID.
     */
    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<Map<String, Object>> deleteEntity(
            @PathVariable String type,
            @PathVariable Long id) {

        log.info("🗑️ Delete request for entity type: {}, ID: {}", type, id);
        String normalizedType = type.trim().toLowerCase();

        return switch (normalizedType) {
            case "customer" -> deleteCustomer(id);
            case "supplier" -> deleteSupplier(id);
            case "product" -> deleteProduct(id);
            case "category" -> deleteCategory(id);
            case "brand" -> deleteBrand(id);
            default -> createErrorResponse("Invalid delete type: " + type, HttpStatus.BAD_REQUEST);
        };
    }

    // -------------------------------
    // DELETE HANDLERS
    // -------------------------------

    private ResponseEntity<Map<String, Object>> deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            return createErrorResponse("Customer not found.", HttpStatus.NOT_FOUND);
        }

        List<String> deps = checkCustomerDependencies(id);
        if (!deps.isEmpty()) {
            return createBlockedResponse("Customer linked with: " + String.join(", ", deps));
        }

        customerRepository.deleteById(id);
        return createSuccessResponse("Customer deleted successfully!");
    }

    private ResponseEntity<Map<String, Object>> deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            return createErrorResponse("Supplier not found.", HttpStatus.NOT_FOUND);
        }

        List<String> deps = checkSupplierDependencies(id);
        if (!deps.isEmpty()) {
            return createBlockedResponse("Supplier linked with: " + String.join(", ", deps));
        }

        supplierRepository.deleteById(id);
        return createSuccessResponse("Supplier deleted successfully!");
    }

    private ResponseEntity<Map<String, Object>> deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return createErrorResponse("Product not found.", HttpStatus.NOT_FOUND);
        }

        List<String> deps = checkProductDependencies(id);
        if (!deps.isEmpty()) {
            return createBlockedResponse("Product linked with: " + String.join(", ", deps));
        }

        productRepository.deleteById(id);
        return createSuccessResponse("Product deleted successfully!");
    }

    private ResponseEntity<Map<String, Object>> deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            return createErrorResponse("Category not found.", HttpStatus.NOT_FOUND);
        }

        List<String> deps = checkCategoryDependencies(id);
        if (!deps.isEmpty()) {
            return createBlockedResponse("Category linked with: " + String.join(", ", deps));
        }

        categoryRepository.deleteById(id);
        return createSuccessResponse("Category deleted successfully!");
    }

    private ResponseEntity<Map<String, Object>> deleteBrand(Long id) {
        if (!brandRepository.existsById(id)) {
            return createErrorResponse("Brand not found.", HttpStatus.NOT_FOUND);
        }

        brandRepository.deleteById(id);
        return createSuccessResponse("Brand deleted successfully!");
    }

    // -------------------------------
    // DEPENDENCY CHECKS
    // -------------------------------

    private List<String> checkCustomerDependencies(Long customerId) {
        List<String> deps = new ArrayList<>();

        if (!saleOrderRepository.findByCustomerId(customerId).isEmpty()) deps.add("Sale Orders");
        if (!paymentRepository.findByCustomerId(customerId).isEmpty()) deps.add("Payments");
        if (!financialAccountRepository.findByCustomerId(customerId).isEmpty()) deps.add("Financial Accounts");

        return deps;
    }

    private List<String> checkSupplierDependencies(Long supplierId) {
        List<String> deps = new ArrayList<>();

        if (!purchaseOrderRepository.findBySupplierId(supplierId).isEmpty()) deps.add("Purchase Orders");
        if (!paymentRepository.findBySupplierId(supplierId).isEmpty()) deps.add("Payments");
        if (!expenseRepository.findByVendorId(supplierId).isEmpty()) deps.add("Expenses");

        return deps;
    }

    private List<String> checkProductDependencies(Long productId) {
        List<String> deps = new ArrayList<>();

        if (!saleOrderDetailsRepository.findByProductId(productId).isEmpty()) deps.add("Sale Orders");
        if (!purchaseOrderItemRepository.findByProductId(productId).isEmpty()) deps.add("Purchase Orders");

        return deps;
    }

    private List<String> checkCategoryDependencies(Long categoryId) {
        List<String> deps = new ArrayList<>();

        if (!productRepository.findByCategoryId(categoryId).isEmpty()) deps.add("Products");

        return deps;
    }

    // -------------------------------
    // RESPONSE HELPERS
    // -------------------------------

    private ResponseEntity<Map<String, Object>> createErrorResponse(String msg, HttpStatus status) {
        return new ResponseEntity<>(Map.of("status", "error", "message", msg), status);
    }

    private ResponseEntity<Map<String, Object>> createSuccessResponse(String msg) {
        return new ResponseEntity<>(Map.of("status", "success", "message", msg), HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> createBlockedResponse(String msg) {
        return new ResponseEntity<>(Map.of("status", "blocked", "message", msg), HttpStatus.CONFLICT);
    }
}
