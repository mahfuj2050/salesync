package com.business.salesync.controller;

import com.business.salesync.models.Brand;
import com.business.salesync.repository.BrandRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/brands"})
public class BrandController {
   @Autowired
   private BrandRepository brandRepository;

   @GetMapping
   public String listBrands(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String search, Model model) {
      Pageable pageable = PageRequest.of(page, size, Sort.by(new String[]{"name"}).ascending());
      Page brandPage;
      if (search != null && !search.trim().isEmpty()) {
         brandPage = this.brandRepository.findByNameContainingIgnoreCase(search, pageable);
      } else {
         brandPage = this.brandRepository.findAll(pageable);
      }

      long totalBrands = this.brandRepository.count();
      long totalProducts = this.brandRepository.findAll().stream().mapToLong((brand) -> {
         return (long)brand.getProducts().size();
      }).sum();
      model.addAttribute("brand_page", brandPage);
      model.addAttribute("totalBrands", totalBrands);
      model.addAttribute("activeBrands", totalBrands);
      model.addAttribute("totalProducts", totalProducts);
      model.addAttribute("currentSearch", search);
      model.addAttribute("currentPage", page);
      model.addAttribute("currentSize", size);
      return "fragments/brands";
   }

   @GetMapping({"/new"})
   public String showCreateForm(Model model) {
      model.addAttribute("brand", new Brand());
      return "fragments/brand_form";
   }

   @GetMapping({"/{id}"})
   public String showEditForm(Model model, @PathVariable Long id) {
      Brand brand = (Brand)this.brandRepository.findById(id).orElseThrow(EntityNotFoundException::new);
      model.addAttribute("brand", brand);
      return "fragments/brand_form";
   }

   @PostMapping({"/save"})
   public String saveBrand(@Valid @ModelAttribute("brand") Brand brand, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
      if (result.hasErrors()) {
         return "brand_form";
      } else {
         try {
            if (brand.getId() == null) {
               Optional<Brand> existingBrand = this.brandRepository.findByNameIgnoreCase(brand.getName());
               if (existingBrand.isPresent()) {
                  result.rejectValue("name", "error.brand", "Brand name already exists!");
                  return "brand_form";
               }
            }

            this.brandRepository.save(brand);
            String message = brand.getId() != null ? "Brand updated successfully!" : "Brand created successfully!";
            redirectAttributes.addFlashAttribute("successMessage", message);
         } catch (Exception var6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving brand: " + var6.getMessage());
         }

         return "redirect:/brands";
      }
   }

   @PostMapping({"/delete"})
   public String deleteBrand(@RequestParam Long id, RedirectAttributes redirectAttributes) {
      try {
         Optional<Brand> brand = this.brandRepository.findById(id);
         if (brand.isPresent()) {
            this.brandRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Brand deleted successfully!");
         } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Brand not found!");
         }
      } catch (Exception var4) {
         redirectAttributes.addFlashAttribute("errorMessage", "Error deleting brand: " + var4.getMessage());
      }

      return "redirect:/brands";
   }
}