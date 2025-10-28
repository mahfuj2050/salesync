package com.business.salesync.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.business.salesync.models.Customer;
import com.business.salesync.repository.CustomerRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 👥 Customer Controller
 * Handles all customer-related operations
 */
@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerRepository customerRepository;

    // ========================================
    // 📄 VIEW PAGES
    // ========================================

    /**
     * Show customer list page
     * GET /customers
     */
    @GetMapping
    public String customersListPage(Model model) {
        log.info("📋 Loading customers list page");
        return "fragments/customers";
    }

    /**
     * Show customer form page (Create new)
     * GET /customers/new
     */
    @GetMapping("/new")
    public String customerFormPage(Model model) {
        log.info("➕ Loading new customer form page");
        
        model.addAttribute("customer", new Customer());
        model.addAttribute("isEdit", false);
        
        return "fragments/customer_form";
    }

    /**
     * Show customer edit form page
     * GET /customers/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String editCustomerFormPage(@PathVariable Long id, Model model) {
        log.info("✏️ Loading edit customer form for ID: {}", id);
        
        try {
            Optional<Customer> customerOpt = customerRepository.findById(id);
            
            if (customerOpt.isEmpty()) {
                log.error("❌ Customer not found with ID: {}", id);
                model.addAttribute("error", "Customer not found");
                return "redirect:/customers";
            }
            
            model.addAttribute("customer", customerOpt.get());
            model.addAttribute("isEdit", true);
            
            return "fragments/customer_form";
            
        } catch (Exception e) {
            log.error("❌ Error loading customer for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading customer");
            return "redirect:/customers";
        }
    }

    // ========================================
    // 🔌 REST API ENDPOINTS
    // ========================================

    /**
     * Get all customers
     * GET /customers/api/list
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllCustomers(
            @RequestParam(required = false) String search) {
        
        log.info("📊 Fetching customers list - Search: {}", search);
        
        try {
            List<Customer> customers;
            
            if (search != null && !search.trim().isEmpty()) {
                customers = customerRepository.searchByKeyword(search.trim());
            } else {
                customers = customerRepository.findAllByOrderByNameAsc();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("customers", customers);
            response.put("count", customers.size());
            
            log.info("✅ Retrieved {} customers", customers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error fetching customers: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get single customer by ID
     * GET /customers/api/{id}
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCustomerById(@PathVariable Long id) {
        log.info("🔍 Fetching customer with ID: {}", id);
        
        try {
            Optional<Customer> customerOpt = customerRepository.findById(id);
            
            if (customerOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Customer not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("customer", customerOpt.get());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error fetching customer: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Create new customer
     * POST /customers/api/create
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCustomer(@Valid @RequestBody Customer customer) {
        log.info("➕ Creating new customer: {}", customer.getName());
        
        try {
            // Check if phone number already exists
            if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Phone number already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Check if email already exists
            if (customerRepository.existsByEmail(customer.getEmail())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Email already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Customer savedCustomer = customerRepository.save(customer);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Customer created successfully");
            response.put("customer", savedCustomer);
            response.put("customerId", savedCustomer.getId());
            
            log.info("✅ Customer created successfully: {}", savedCustomer.getName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error creating customer: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error creating customer: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update existing customer
     * PUT /customers/api/update/{id}
     */
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody Customer customer) {
        
        log.info("✏️ Updating customer ID: {}", id);
        
        try {
            Optional<Customer> existingCustomerOpt = customerRepository.findById(id);
            
            if (existingCustomerOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Customer not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Customer existingCustomer = existingCustomerOpt.get();
            
            // Check if phone number is changed and already exists
            if (!existingCustomer.getPhoneNumber().equals(customer.getPhoneNumber()) &&
                customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Phone number already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Check if email is changed and already exists
            if (!existingCustomer.getEmail().equals(customer.getEmail()) &&
                customerRepository.existsByEmail(customer.getEmail())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Email already exists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Update fields
            existingCustomer.setName(customer.getName());
            existingCustomer.setPhoneNumber(customer.getPhoneNumber());
            existingCustomer.setEmail(customer.getEmail());
            existingCustomer.setAddress(customer.getAddress());
            
            Customer updatedCustomer = customerRepository.save(existingCustomer);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Customer updated successfully");
            response.put("customer", updatedCustomer);
            
            log.info("✅ Customer updated successfully: {}", updatedCustomer.getName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error updating customer: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error updating customer: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete customer
     * DELETE /customers/api/delete/{id}
     */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable Long id) {
        log.info("🗑️ Deleting customer ID: {}", id);
        
        try {
            Optional<Customer> customerOpt = customerRepository.findById(id);
            
            if (customerOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Customer not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            customerRepository.deleteById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Customer deleted successfully");
            
            log.info("✅ Customer deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error deleting customer: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error deleting customer: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get customer statistics
     * GET /customers/api/stats
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCustomerStats() {
        log.info("📊 Fetching customer statistics");
        
        try {
            long totalCustomers = customerRepository.countTotalCustomers();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCustomers", totalCustomers);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error fetching stats: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Search customers
     * GET /customers/api/search
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address) {
        
        log.info("🔍 Searching customers");
        
        try {
            List<Customer> customers = customerRepository.searchCustomers(name, phone, email, address);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("customers", customers);
            response.put("count", customers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error searching customers: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}