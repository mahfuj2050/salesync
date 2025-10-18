package com.business.salesync.controller;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.business.salesync.models.Brand;
import com.business.salesync.models.Category;
import com.business.salesync.models.Product;
import com.business.salesync.models.Supplier;
import com.business.salesync.repository.BrandRepository;
import com.business.salesync.repository.CategoryRepository;
import com.business.salesync.repository.ProductRepository;
import com.business.salesync.repository.SupplierRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;



@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private SupplierRepository supplierRepository;

	/*-
    @GetMapping("/products")
    public String products(Model model,
                           @RequestParam(required = false, name = "page", defaultValue = "1") int page,
                           @RequestParam(required = false, name = "size", defaultValue = "50") int size) {

        if (page > 0) {
            page--;
        }
        Pageable pageInfo = PageRequest.of(page, size);

        Page<Product> productPage = productRepository.findAll(pageInfo);
        model.addAttribute("product_page", productPage);

        return "fragments/products";
    }*/
    
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long brand,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        
        System.out.println("=== PRODUCTS CONTROLLER CALLED ===");
        System.out.println("Page: " + page + ", Size: " + size);
        System.out.println("Search: " + search);
        System.out.println("Category: " + category + ", Brand: " + brand);
        System.out.println("Stock Status: " + stockStatus);
        System.out.println("Sort: " + sort + ", Direction: " + direction);
        
        // Build dynamic query based on filters
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<Product> productPage = productRepository.findWithFilters(search, category, brand, stockStatus, pageable);
        
        // Calculate summary statistics
        long totalProducts = productRepository.count();
        long lowStockCount = productRepository.countLowStockProducts();
        BigDecimal totalStockValue = productRepository.calculateTotalStockValue();
        
        // Add all parameters to model for thymeleaf - handle nulls
        model.addAttribute("product_page", productPage);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("totalStockValue", totalStockValue != null ? totalStockValue : BigDecimal.ZERO);
        
        // Add filter parameters to maintain state - handle nulls properly
        model.addAttribute("currentSearch", search != null ? search : "");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentBrand", brand);
        model.addAttribute("currentStockStatus", stockStatus != null ? stockStatus : "");
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDirection", direction);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        
        System.out.println("Found " + productPage.getTotalElements() + " products");
        System.out.println("Current Page: " + productPage.getNumber() + ", Total Pages: " + productPage.getTotalPages());
        System.out.println("===============================");
        
        return "fragments/products";
    }
    
	/*-
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,  // Add default size
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long brand,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        
        System.out.println("=== PRODUCTS CONTROLLER CALLED ===");
        System.out.println("Page: " + page + ", Size: " + size);
        System.out.println("Search: " + search);
        System.out.println("Category: " + category + ", Brand: " + brand);
        System.out.println("Stock Status: " + stockStatus);
        System.out.println("Sort: " + sort + ", Direction: " + direction);
        
        // Build dynamic query based on filters
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<Product> productPage = productRepository.findWithFilters(search, category, brand, stockStatus, pageable);
        
        // Calculate summary statistics
        long totalProducts = productRepository.count();
        long lowStockCount = productRepository.countLowStockProducts();
        BigDecimal totalStockValue = productRepository.calculateTotalStockValue();
        
        // Add all parameters to model for thymeleaf
        model.addAttribute("product_page", productPage);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("totalStockValue", totalStockValue != null ? totalStockValue : BigDecimal.ZERO);
        
        // Add filter parameters to maintain state - WITH DEFAULTS
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentBrand", brand);
        model.addAttribute("currentStockStatus", stockStatus);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDirection", direction);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);  // This was missing!
        
        System.out.println("Found " + productPage.getTotalElements() + " products");
        System.out.println("===============================");
        
        return "fragments/products";
    }*/

    @GetMapping(value = {"/product", "/product/{id}"})
    public String viewProduct(Model model, @PathVariable(required = false) Long id) {

        if (id != null) {
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent()) {
                model.addAttribute("product", product.get());
            } else {
                throw new EntityNotFoundException("Product not found with id " + id);
            }
        } else {
            model.addAttribute("product", new Product());
        }

        // Add dropdown data
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());

        return "fragments/product_form";
    }

    @PostMapping(value = {"/product"})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String createUpdateProduct(@Valid @ModelAttribute("product") Product product,
                                      BindingResult result,
                                      Model model) {

        System.out.println("=== FORM SUBMISSION START ===");
        System.out.println("ID: " + product.getId());
        System.out.println("Name: " + product.getName());
        System.out.println("SKU: " + product.getSku());
        System.out.println("Quantity: " + product.getQuantity());
        System.out.println("Cost Price: " + product.getCostPrice());
        System.out.println("Selling Price: " + product.getSellingPrice());
        System.out.println("Manufacture Date: " + product.getManufactureDate());
        System.out.println("Expiry Date: " + product.getExpiryDate());
        System.out.println("Category ID: " + 
            (product.getCategory() != null ? product.getCategory().getId() : "null"));
        System.out.println("Brand ID: " + 
            (product.getBrand() != null ? product.getBrand().getId() : "null"));
        System.out.println("Supplier ID: " + 
            (product.getSupplier() != null ? product.getSupplier().getId() : "null"));
        System.out.println("=============================");

        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("suppliers", supplierRepository.findAll());
            return "fragments/product_form";
        }

        try {
            // Fix category/brand/supplier mapping manually
            // IMPORTANT: Only set if ID is not null and greater than 0
            if (product.getCategory() != null && product.getCategory().getId() != null && product.getCategory().getId() > 0) {
                Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + product.getCategory().getId()));
                product.setCategory(category);
            } else {
                product.setCategory(null); // Explicitly set to null if no category selected
            }
            
            if (product.getBrand() != null && product.getBrand().getId() != null && product.getBrand().getId() > 0) {
                Brand brand = brandRepository.findById(product.getBrand().getId())
                    .orElseThrow(() -> new RuntimeException("Brand not found with id: " + product.getBrand().getId()));
                product.setBrand(brand);
            } else {
                product.setBrand(null); // Explicitly set to null if no brand selected
            }
            
            if (product.getSupplier() != null && product.getSupplier().getId() != null && product.getSupplier().getId() > 0) {
                Supplier supplier = supplierRepository.findById(product.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + product.getSupplier().getId()));
                product.setSupplier(supplier);
            } else {
                product.setSupplier(null); // Explicitly set to null if no supplier selected
            }

            // For new products, let the database trigger handle SKU/barcode generation
            if (product.getId() == null) {
                // Set empty values to let trigger generate them
                product.setSku("");
                product.setBarcode("");
                product.setBatchNo("");
            }

            productRepository.save(product);
            System.out.println("✅ Product saved successfully!");
            return "redirect:/products";
            
        } catch (Exception e) {
            System.out.println("❌ Error saving product: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error saving product: " + e.getMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("suppliers", supplierRepository.findAll());
            return "fragments/product_form";
        }
    }

    @PostMapping("/products/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteProduct(@RequestParam("id") Long id) {
        productRepository.deleteById(id);
        return "redirect:/products";
    }
}
