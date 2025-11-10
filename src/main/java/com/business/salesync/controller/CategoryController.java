package com.business.salesync.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.salesync.models.Category;
import com.business.salesync.repository.CategoryRepository;



@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // List all categories
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "fragments/categories";
    }

    // Show form to add new category
    @GetMapping("/new")
    public String showCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "fragments/category_form";
    }

    // Save or Update category
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category,
                               RedirectAttributes redirectAttributes) {
        
        // If category has an ID, it's an update operation
        if (category.getId() != null) {
            // Fetch existing category to ensure it exists
            Category existingCategory = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + category.getId()));
            
            // Update the existing category with new values
            existingCategory.setName(category.getName());
            existingCategory.setDescription(category.getDescription());
            
            categoryRepository.save(existingCategory);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
        } else {
            // It's a new category
            categoryRepository.save(category);
            redirectAttributes.addFlashAttribute("success", "Category saved successfully!");
        }
        
        return "redirect:/categories";
    }

    // Delete category
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        return "redirect:/categories";
    }

    // Edit category
    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));
        model.addAttribute("category", category);
        return "fragments/category_form";
    }
}
