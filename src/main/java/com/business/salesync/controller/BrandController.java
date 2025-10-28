package com.business.salesync.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.salesync.models.Brand;
import com.business.salesync.models.Supplier;
import com.business.salesync.repository.BrandRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import java.util.Optional;


@Controller
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private BrandRepository brandRepository;

    // List all brands with pagination and search
    @GetMapping
    public String listBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Brand> brandPage;
        
        if (search != null && !search.trim().isEmpty()) {
            brandPage = brandRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            brandPage = brandRepository.findAll(pageable);
        }
        
        // Calculate statistics - using collection sizes
        long totalBrands = brandRepository.count();
        long activeBrands = totalBrands;
        
        // Count total products by iterating through brands
        long totalProducts = brandRepository.findAll().stream()
                .mapToLong(brand -> brand.getProducts().size())
                .sum();
        
        model.addAttribute("brand_page", brandPage);
        model.addAttribute("totalBrands", totalBrands);
        model.addAttribute("activeBrands", activeBrands);
        model.addAttribute("totalProducts", totalProducts);
        
        // Add filter parameters to maintain state
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        
        return "fragments/brands";
    }

    // Show form for creating new brand
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("brand", new Brand());
        return "fragments/brand_form";
    }
    
    @GetMapping("/{id}")
    public String showEditForm(Model model, @PathVariable Long id) {
    	Brand brand = brandRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        model.addAttribute("brand", brand);
        return "fragments/brand_form";
    }

    // Show form for editing existing brand
//    @GetMapping("/{id}")
//    public String showEditForm(@PathVariable Long id, Model model) {
//        Optional<Brand> brand = brandRepository.findById(id);
//        if (brand.isPresent()) {
//            model.addAttribute("brand", brand.get());
//            return "brand_form";
//        } else {
//            return "redirect:/brands";
//        }
//    }

    // Save or update brand
    @PostMapping("/save")
    public String saveBrand(@Valid @ModelAttribute("brand") Brand brand,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "brand_form";
        }
        
        try {
            // Check if brand name already exists (for new brands)
            if (brand.getId() == null) {
                Optional<Brand> existingBrand = brandRepository.findByNameIgnoreCase(brand.getName());
                if (existingBrand.isPresent()) {
                    result.rejectValue("name", "error.brand", "Brand name already exists!");
                    return "brand_form";
                }
            }
            
            brandRepository.save(brand);
            String message = brand.getId() != null ? 
                "Brand updated successfully!" : "Brand created successfully!";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error saving brand: " + e.getMessage());
        }
        
        return "redirect:/brands";
    }

    // Delete brand
    @PostMapping("/delete")
    public String deleteBrand(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Brand> brand = brandRepository.findById(id);
            if (brand.isPresent()) {
                brandRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Brand deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Brand not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting brand: " + e.getMessage());
        }
        
        return "redirect:/brands";
    }
}