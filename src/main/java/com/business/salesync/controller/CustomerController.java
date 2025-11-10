package com.business.salesync.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.business.salesync.models.Customer;
import com.business.salesync.repository.CustomerRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 👥 Customer Controller
 * Handles all customer-related operations.
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

    /** Show customer list page */
    @GetMapping
    public String customersListPage(Model model) {
        log.info("📋 Loading customers list page");
        return "fragments/customers";
    }

    /** Show form page for creating a new customer */
    @GetMapping("/new")
    public String customerFormPage(Model model) {
        log.info("➕ Loading new customer form page");
        model.addAttribute("customer", new Customer());
        model.addAttribute("isEdit", false);
        return "fragments/customer_form";
    }

    /** Show edit form for existing customer */
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

    /** Get all customers (with optional search) */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllCustomers(
            @RequestParam(required = false) String search) {

        log.info("📊 Fetching customers list - Search: {}", search);
        Map<String, Object> response = new HashMap<>();

        try {
            List<Customer> customers = (search != null && !search.trim().isEmpty())
                    ? customerRepository.searchByKeyword(search.trim())
                    : customerRepository.findAllByOrderByNameAsc();

            response.put("success", true);
            response.put("customers", customers);
            response.put("count", customers.size());
            log.info("✅ Retrieved {} customers", customers.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error fetching customers: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /** Get single customer by ID */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCustomerById(@PathVariable Long id) {
        log.info("🔍 Fetching customer with ID: {}", id);
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Customer> customerOpt = customerRepository.findById(id);
            if (customerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Customer not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("customer", customerOpt.get());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error fetching customer: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /** Create new customer */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCustomer(
            @Valid @RequestBody Customer customer, BindingResult bindingResult) {

        log.info("➕ Creating new customer: {}", customer.getName());
        Map<String, Object> response = new HashMap<>();

        try {
            cleanEmptyFields(customer);

            if (bindingResult.hasErrors()) {
                response.put("success", false);
                response.put("message", "Validation failed");
                response.put("errors", bindingResult.getFieldErrors().stream()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .collect(Collectors.toList()));
                return ResponseEntity.badRequest().body(response);
            }

            if (customer.getPhoneNumber() != null &&
                    customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
                return error("Phone number already exists", HttpStatus.BAD_REQUEST);
            }

            if (customer.getEmail() != null &&
                    customerRepository.existsByEmail(customer.getEmail())) {
                return error("Email already exists", HttpStatus.BAD_REQUEST);
            }

            Customer savedCustomer = customerRepository.save(customer);
            response.put("success", true);
            response.put("message", "Customer created successfully");
            response.put("customer", savedCustomer);
            response.put("customerId", savedCustomer.getId());
            log.info("✅ Customer created successfully: {}", savedCustomer.getName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error creating customer: {}", e.getMessage(), e);
            return error("Error creating customer: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /** Update existing customer */
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody Customer customer,
            BindingResult bindingResult) {

        log.info("✏️ Updating customer ID: {}", id);
        Map<String, Object> response = new HashMap<>();

        try {
            cleanEmptyFields(customer);

            if (bindingResult.hasErrors()) {
                response.put("success", false);
                response.put("message", "Validation failed");
                response.put("errors", bindingResult.getFieldErrors().stream()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .collect(Collectors.toList()));
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Customer> existingOpt = customerRepository.findById(id);
            if (existingOpt.isEmpty()) {
                return error("Customer not found", HttpStatus.NOT_FOUND);
            }

            Customer existing = existingOpt.get();

            if (customer.getPhoneNumber() != null &&
                    !customer.getPhoneNumber().equals(existing.getPhoneNumber()) &&
                    customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
                return error("Phone number already exists", HttpStatus.BAD_REQUEST);
            }

            if (customer.getEmail() != null &&
                    !customer.getEmail().equals(existing.getEmail()) &&
                    customerRepository.existsByEmail(customer.getEmail())) {
                return error("Email already exists", HttpStatus.BAD_REQUEST);
            }

            existing.setName(customer.getName());
            existing.setPhoneNumber(customer.getPhoneNumber());
            existing.setEmail(customer.getEmail());
            existing.setAddress(customer.getAddress());

            Customer updated = customerRepository.save(existing);
            response.put("success", true);
            response.put("message", "Customer updated successfully");
            response.put("customer", updated);

            log.info("✅ Customer updated successfully: {}", updated.getName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error updating customer: {}", e.getMessage(), e);
            return error("Error updating customer: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /** Delete customer */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable Long id) {
        log.info("🗑️ Deleting customer ID: {}", id);

        try {
            if (customerRepository.findById(id).isEmpty()) {
                return error("Customer not found", HttpStatus.NOT_FOUND);
            }

            customerRepository.deleteById(id);
            log.info("✅ Customer deleted successfully");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Customer deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error deleting customer: {}", e.getMessage(), e);
            return error("Error deleting customer: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Get customer statistics */
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
            log.error("❌ Error fetching stats: {}", e.getMessage(), e);
            return error("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Search customers with filters */
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
            log.error("❌ Error searching customers: {}", e.getMessage(), e);
            return error("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================
    // 🧩 UTILITIES
    // ========================================

    private void cleanEmptyFields(Customer c) {
        if (c.getPhoneNumber() != null && c.getPhoneNumber().trim().isEmpty()) c.setPhoneNumber(null);
        if (c.getEmail() != null && c.getEmail().trim().isEmpty()) c.setEmail(null);
        if (c.getAddress() != null && c.getAddress().trim().isEmpty()) c.setAddress(null);
    }

    private ResponseEntity<Map<String, Object>> error(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
}
