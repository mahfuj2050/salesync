package com.business.salesync.controller;

import com.business.salesync.repository.BrandRepository;
import com.business.salesync.repository.CategoryRepository;
import com.business.salesync.repository.CustomerRepository;
import com.business.salesync.repository.ExpenseItemRepository;
import com.business.salesync.repository.ExpenseRepository;
import com.business.salesync.repository.FinancialAccountRepository;
import com.business.salesync.repository.OrderDetailsRepository;
import com.business.salesync.repository.OrderRepository;
import com.business.salesync.repository.PaymentRepository;
import com.business.salesync.repository.ProductRepository;
import com.business.salesync.repository.PurchaseOrderItemRepository;
import com.business.salesync.repository.PurchaseOrderRepository;
import com.business.salesync.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/delete")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DeleteController {

    // Inject all repositories
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
     * 🗑️ Universal delete endpoint for all entity types.
     * Example: DELETE /api/delete/entity?type=customer&id=5
     */
    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<Map<String, Object>> deleteEntity(
            @PathVariable String type,
            @PathVariable Long id)
    {
        log.info("🗑️ Delete request for entity type: {}, ID: {}", type, id);

        type = type.trim().toLowerCase();

        switch (type) {
            case "customer":
                if (!customerRepository.existsById(id))
                    return createErrorResponse("Customer not found.", HttpStatus.NOT_FOUND);

                List<String> custDeps = checkCustomerDependencies(id);
                if (!custDeps.isEmpty())
                    return createBlockedResponse("Customer linked with: " + String.join(", ", custDeps));

                customerRepository.deleteById(id);
                return createSuccessResponse("Customer deleted successfully!");

            case "supplier":
                if (!supplierRepository.existsById(id))
                    return createErrorResponse("Supplier not found.", HttpStatus.NOT_FOUND);

                List<String> suppDeps = checkSupplierDependencies(id);
                if (!suppDeps.isEmpty())
                    return createBlockedResponse("Supplier linked with: " + String.join(", ", suppDeps));

                supplierRepository.deleteById(id);
                return createSuccessResponse("Supplier deleted successfully!");

            case "product":
                if (!productRepository.existsById(id))
                    return createErrorResponse("Product not found.", HttpStatus.NOT_FOUND);

                List<String> prodDeps = checkProductDependencies(id);
                if (!prodDeps.isEmpty())
                    return createBlockedResponse("Product linked with: " + String.join(", ", prodDeps));

                productRepository.deleteById(id);
                return createSuccessResponse("Product deleted successfully!");

            case "category":
                if (!categoryRepository.existsById(id))
                    return createErrorResponse("Category not found.", HttpStatus.NOT_FOUND);

                List<String> catDeps = checkCategoryDependencies(id);
                if (!catDeps.isEmpty())
                    return createBlockedResponse("Category linked with: " + String.join(", ", catDeps));

                categoryRepository.deleteById(id);
                return createSuccessResponse("Category deleted successfully!");

                
            case "brand":
                if (!brandRepository.existsById(id))
                    return createErrorResponse("Brand not found.", HttpStatus.NOT_FOUND);

                brandRepository.deleteById(id);
                return createSuccessResponse("Brand deleted successfully!");

            default:
                return createErrorResponse("Invalid delete type: " + type, HttpStatus.BAD_REQUEST);
        }
    }

    // =============== Dependency Checkers ===============
    private List<String> checkCustomerDependencies(Long customerId) {
        List<String> list = new ArrayList<>();
        if (!saleOrderRepository.findByCustomerId(customerId).isEmpty()) list.add("Sale Orders");
        if (!paymentRepository.findByCustomerId(customerId).isEmpty()) list.add("Payments");
        if (!financialAccountRepository.findByCustomerId(customerId).isEmpty()) list.add("Financial Accounts");
        return list;
    }

    private List<String> checkSupplierDependencies(Long supplierId) {
        List<String> list = new ArrayList<>();
        if (!purchaseOrderRepository.findBySupplierId(supplierId).isEmpty()) list.add("Purchase Orders");
        if (!paymentRepository.findBySupplierId(supplierId).isEmpty()) list.add("Payments");
        if (!expenseRepository.findByVendorId(supplierId).isEmpty()) list.add("Expenses");
        return list;
    }

    private List<String> checkProductDependencies(Long productId) {
        List<String> list = new ArrayList<>();
        if (!saleOrderDetailsRepository.findByProductId(productId).isEmpty()) list.add("Sale Orders");
        if (!purchaseOrderItemRepository.findByProductId(productId).isEmpty()) list.add("Purchase Orders");
        //if (!expenseItemRepository.findByProductId(productId).isEmpty()) list.add("Expense Items");
        return list;
    }
    
    private List<String> checkCategoryDependencies(Long categoryId) {
        List<String> list = new ArrayList<>();
        if (!productRepository.findByCategoryId(categoryId).isEmpty()) list.add("Products");
        return list;
    }


    // =============== JSON Response Helpers ===============
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
