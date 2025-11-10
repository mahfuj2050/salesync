package com.business.salesync.controller;

// --- Standard Java Imports ---
import java.util.Arrays;
import java.util.Optional;

// --- Spring Framework Imports ---
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

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
@RequestMapping("/product/manage")
@RequiredArgsConstructor // Injects final fields via constructor
public class ProductManageController {

    // --- Repositories (Injected via Constructor) ---
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SupplierRepository supplierRepository;

    // ----------------------------------------------------------------------
    //                           PRODUCT FORM DISPLAY
    // ----------------------------------------------------------------------

    /**
     * Displays the product form for creation (no ID) or editing (with ID).
     * It also builds a 'returnUrl' to preserve filter state upon saving.
     * Maps to: GET /product/manage/form or GET /product/manage/form/{id}
     */
    @GetMapping({"/form", "/form/{id}"})
    public String showProductForm(
            Model model,
            @PathVariable(required = false) Long id,
            // Parameters for building the return URL to the product listing
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long brand,
            @RequestParam(required = false) Long supplier,
            @RequestParam(required = false) String stockStatus) {

        log.info("Displaying product form. ID: {}", id != null ? id : "New");
        
        // 1. Fetch Product or create new instance
        Product product = id != null 
            ? productRepository.findById(id).orElseThrow(() -> {
                log.error("Product not found with ID: {}", id);
                return new EntityNotFoundException("Product not found with id " + id);
            }) 
            : new Product();

        model.addAttribute("product", product);
        
        // 2. Add dropdown data
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
        
        // Add hardcoded list of colors (as found in original)
        model.addAttribute("colors", Arrays.asList(
            "Red", "Blue", "Green", "Black", "White", "Silver", "Gray", 
            "Multicolor", "Yellow", "Orange", "Purple", "Pink"));

        // 3. Build return URL to redirect user back to the filtered list
        String returnUrl = UriComponentsBuilder.fromPath("/products")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort)
                .queryParam("direction", direction)
                // Use empty string if null to ensure parameter is included
                .queryParam("search", Optional.ofNullable(search).orElse(""))
                .queryParam("category", Optional.ofNullable(category).orElse(null))
                .queryParam("brand", Optional.ofNullable(brand).orElse(null))
                .queryParam("supplier", Optional.ofNullable(supplier).orElse(null))
                .queryParam("stockStatus", Optional.ofNullable(stockStatus).orElse(""))
                .build()
                .toUriString();

        log.debug("Generated Return URL: {}", returnUrl);
        model.addAttribute("returnUrl", returnUrl);
        
        return "fragments/product-edit";
    }

    // ----------------------------------------------------------------------
    //                           PRODUCT SAVE/UPDATE
    // ----------------------------------------------------------------------

    /**
     * Handles form submission to save or update a product.
     * Maps to: POST /product/manage/save
     */
    @PostMapping("/save")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String saveProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            Model model) {

        log.info("Saving product. ID: {}, Name: '{}'", product.getId(), product.getName());
        
        // Define default return URL if none is provided
        String finalReturnUrl = returnUrl != null && !returnUrl.trim().isEmpty() ? returnUrl : "/products";

        // 1. Handle Validation Errors
        if (result.hasErrors()) {
            log.warn("Validation errors found for product: {}", result.getAllErrors());
            // Re-populate dropdowns and return URL before returning to the form
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("suppliers", supplierRepository.findAll());
            model.addAttribute("returnUrl", finalReturnUrl);
            return "fragments/product-edit";
        } 
        
        try {
            // 2. Fix entity mapping (manually fetch managed entities)
            
            // Brand mapping
            if (product.getBrand() != null && product.getBrand().getId() != null) {
                Brand brand = brandRepository.findById(product.getBrand().getId())
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
                product.setBrand(brand);
            } else {
                product.setBrand(null);
            }

            // Category mapping
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
                product.setCategory(category);
            } else {
                product.setCategory(null);
            }

            // Supplier mapping
            if (product.getSupplier() != null && product.getSupplier().getId() != null) {
                Supplier supplier = supplierRepository.findById(product.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
                product.setSupplier(supplier);
            } else {
                product.setSupplier(null);
            }

            // 3. Save and redirect
            productRepository.save(product);
            log.info("✅ Product saved successfully! Redirecting to: {}", finalReturnUrl);
            
            return "redirect:" + finalReturnUrl;

        } catch (Exception e) {
            log.error("❌ Error saving product: {}", e.getMessage(), e);
            
            model.addAttribute("error", "Error saving product: " + e.getMessage());
            // Re-populate dropdowns on error
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("suppliers", supplierRepository.findAll());
            model.addAttribute("returnUrl", finalReturnUrl);
            
            return "fragments/product-edit";
        }
    }
}