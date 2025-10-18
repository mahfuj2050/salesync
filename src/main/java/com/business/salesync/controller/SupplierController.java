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

import com.business.salesync.models.Supplier;
import com.business.salesync.repository.SupplierRepository;

import jakarta.validation.Valid;

import java.util.Optional;


@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierRepository supplierRepository;

    // List all suppliers with pagination and search
    @GetMapping
    public String listSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("supplierName").ascending());
        Page<Supplier> supplierPage;
        
        if (search != null && !search.trim().isEmpty()) {
            // Search by supplier name, email, or phone
            supplierPage = supplierRepository.findBySearchTerm(search, pageable);
        } else {
            supplierPage = supplierRepository.findAll(pageable);
        }
        
        // Calculate statistics
        long totalSuppliers = supplierRepository.count();
        long activeSuppliers = totalSuppliers; // You can add active/inactive logic if needed
        long totalProducts = supplierRepository.countTotalProducts();
        
        model.addAttribute("supplier_page", supplierPage);
        model.addAttribute("totalSuppliers", totalSuppliers);
        model.addAttribute("activeSuppliers", activeSuppliers);
        model.addAttribute("totalProducts", totalProducts);
        
        // Add filter parameters to maintain state
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        
        return "fragments/suppliers";
    }

    // Show form for creating new supplier
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "fragments/supplier_form";
    }

    // Show form for editing existing supplier
    @GetMapping("/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Supplier> supplier = supplierRepository.findById(id);
        if (supplier.isPresent()) {
            model.addAttribute("supplier", supplier.get());
            return "supplier_form";
        } else {
            return "redirect:/suppliers";
        }
    }

    // Save or update supplier
    @PostMapping("/save")
    public String saveSupplier(@Valid @ModelAttribute("supplier") Supplier supplier,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "supplier_form";
        }
        
        try {
            supplierRepository.save(supplier);
            String message = supplier.getId() != null ? 
                "Supplier updated successfully!" : "Supplier created successfully!";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error saving supplier: " + e.getMessage());
        }
        
        return "redirect:/suppliers";
    }

    // Delete supplier
    @PostMapping("/delete")
    public String deleteSupplier(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Supplier> supplier = supplierRepository.findById(id);
            if (supplier.isPresent()) {
                supplierRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Supplier deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Supplier not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting supplier: " + e.getMessage());
        }
        
        return "redirect:/suppliers";
    }
}