package com.business.salesync.controller;

// --- Standard Java Imports (Organized) ---
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// --- Spring Framework Imports ---
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// --- Third-Party/Utility Imports ---
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// --- Application Model and Repository Imports ---
import com.business.salesync.models.Brand;
import com.business.salesync.models.Category;
import com.business.salesync.models.Product;
import com.business.salesync.models.Supplier;
import com.business.salesync.repository.BrandRepository;
import com.business.salesync.repository.CategoryRepository;
import com.business.salesync.repository.ProductRepository;
import com.business.salesync.repository.SupplierRepository;

// --- Jakarta/JPA Imports ---
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor // Injects all final fields via constructor
public class ProductController {

    // --- Repositories (Injected via Constructor) ---
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SupplierRepository supplierRepository;

    // ----------------------------------------------------------------------
    //                           PRODUCT LISTING & FILTERING
    // ----------------------------------------------------------------------

    /**
     * Handles the product list view, supporting pagination, searching, and filtering.
     * Maps to: GET /products
     */
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long brand,
            @RequestParam(required = false) Long supplier,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        // Log incoming request parameters
        log.info("Products listing called. Page: {}, Size: {}, Search: '{}', Filters (Cat:{}, Brand:{}, Supp:{}).", 
                 page, size, search, category, brand, supplier);

        // 1. Build Pageable object for sorting and pagination
        Direction sortDirection = Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // 2. Fetch the paginated and filtered product data
        Page<Product> productPage = productRepository.findWithFilters(search, category, brand, supplier, stockStatus, pageable);

        // 3. Fetch summary statistics
        long totalProducts = productRepository.count();
        long lowStockCount = productRepository.countLowStockProducts();
        BigDecimal totalStockValue = productRepository.calculateTotalStockValue();

        // 4. Populate Model with fetched data
        model.addAttribute("product_page", productPage);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("totalStockValue", totalStockValue != null ? totalStockValue : BigDecimal.ZERO);

        // 5. Populate Model with current filter/sort state for UI persistence
        model.addAttribute("currentSearch", Optional.ofNullable(search).orElse(""));
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentBrand", brand);
        model.addAttribute("currentSupplier", supplier);
        model.addAttribute("currentStockStatus", Optional.ofNullable(stockStatus).orElse(""));
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDirection", direction);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);

        log.info("Found {} total products (filtered count: {}).", totalProducts, productPage.getTotalElements());

        return "fragments/products";
    }

    // ----------------------------------------------------------------------
    //                           PRODUCT FORM (CREATE/EDIT)
    // ----------------------------------------------------------------------

    /**
     * Displays the product creation form (id=null) or the edit form (id present).
     * Maps to: GET /product or GET /product/{id}
     */
    @GetMapping(value = {"/product", "/product/{id}"})
    public String viewProduct(Model model, @PathVariable(required = false) Long id) {

        Product product;
        if (id != null) {
            // Edit existing product
            product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Product not found with ID: {}", id);
                        return new EntityNotFoundException("Product not found with id " + id);
                    });
        } else {
            // Create new product
            product = new Product();
        }

        model.addAttribute("product", product);

        // Add dropdown data for associations
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());

        return "fragments/product_form";
    }

    // ----------------------------------------------------------------------
    //                           PRODUCT SAVE/UPDATE
    // ----------------------------------------------------------------------

    /**
     * Handles form submission for creating or updating a product.
     * Maps to: POST /product
     */
    @PostMapping("/product")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Security check
    public String createUpdateProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            Model model) {

        log.info("Product form submission received. ID: {}, Name: '{}', SKU: '{}', Quantity: {}.", 
                 product.getId(), product.getName(), product.getSku(), product.getQuantity());

        // 1. Handle Validation Errors
        if (result.hasErrors()) {
            log.warn("Validation errors found for product ID {}: {}", product.getId(), result.getAllErrors());
            // Re-populate dropdowns before returning to the form
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("suppliers", supplierRepository.findAll());
            return "fragments/product_form";
        }

        try {
            // 2. Fix entity mapping (manually fetch managed entities)
            
            // Helper function to find and set associated entities based on ID
            // This is necessary because the form only sends an ID within the associated object.
            
            // Category mapping
            if (product.getCategory() != null && product.getCategory().getId() != null && product.getCategory().getId() > 0) {
                Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + product.getCategory().getId()));
                product.setCategory(category);
            } else {
                product.setCategory(null); // Explicitly set to null if no category selected/ID is invalid
            }
            
            // Brand mapping
            if (product.getBrand() != null && product.getBrand().getId() != null && product.getBrand().getId() > 0) {
                Brand brand = brandRepository.findById(product.getBrand().getId())
                    .orElseThrow(() -> new RuntimeException("Brand not found with id: " + product.getBrand().getId()));
                product.setBrand(brand);
            } else {
                product.setBrand(null); // Explicitly set to null if no brand selected
            }
            
            // Supplier mapping
            if (product.getSupplier() != null && product.getSupplier().getId() != null && product.getSupplier().getId() > 0) {
                Supplier supplier = supplierRepository.findById(product.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + product.getSupplier().getId()));
                product.setSupplier(supplier);
            } else {
                product.setSupplier(null); // Explicitly set to null if no supplier selected
            }

            // 3. Handle generated fields for new products
            // The original logic assumes a DB trigger handles SKU/Barcode generation if fields are empty
            if (product.getId() == null) {
                product.setSku("");
                product.setBarcode("");
                product.setBatchNo("");
            }

            // 4. Save and redirect
            productRepository.save(product);
            log.info("Product saved successfully. ID: {}", product.getId());
            return "redirect:/products";

        } catch (Exception e) {
            log.error("Error saving product: {}", e.getMessage(), e);
            model.addAttribute("error", "Error saving product: " + e.getMessage());
            // Re-populate dropdowns on error
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("suppliers", supplierRepository.findAll());
            return "fragments/product_form";
        }
    }

    // ----------------------------------------------------------------------
    //                              PRODUCT DELETE
    // ----------------------------------------------------------------------

    /**
     * Handles deletion of a product by ID.
     * Maps to: POST /products/delete
     */
    @PostMapping("/products/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Security check
    public String deleteProduct(@RequestParam("id") Long id) {
        log.info("Attempting to delete product ID: {}", id);
        productRepository.deleteById(id);
        log.info("Product ID {} deleted successfully.", id);
        return "redirect:/products";
    }
}