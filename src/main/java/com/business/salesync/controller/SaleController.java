package com.business.salesync.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.business.salesync.models.Customer;
import com.business.salesync.models.OrderDetails;
import com.business.salesync.models.Product;
import com.business.salesync.models.SalesOrder;
import com.business.salesync.repository.CustomerRepository;
import com.business.salesync.repository.OrderDetailsRepository;
import com.business.salesync.repository.OrderRepository;
import com.business.salesync.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;



@Controller
public class SaleController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @GetMapping("/orders")
    public String orders(Model model, @RequestParam(required = false, name = "page", defaultValue = "1") int page,
            @RequestParam(required = false, name = "size", defaultValue = "50") int size) {

        if (page > 0) {
            page--;
        }
        Pageable pageInfo = PageRequest.of(page, size);

        Page<SalesOrder> orderPage = orderRepository.findAll(pageInfo);
        model.addAttribute("order_page", orderPage);

        return "fragments/orders";
    }

    @GetMapping(value = { "/order", "/order/{id}" })
    public String viewOrder(Model model, @PathVariable(required = false) Long id) throws JsonProcessingException {

        List<Customer> customers = customerRepository.findAll();
        List<Product> products = productRepository.findAll();

        model.addAttribute("customers", customers);

        // Convert products to JSON-safe list using DTO or Map
        List<Map<String, Object>> productsJsonList = products.stream()
            .map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId());
                map.put("name", p.getName());
                map.put("price", p.getSellingPrice());
                return map;
            }).collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        String productsJson = mapper.writeValueAsString(productsJsonList);
        model.addAttribute("productsJson", productsJson);

        if (id != null) {
            Optional<SalesOrder> order = orderRepository.findById(id);
            if (order.isPresent()) {
                model.addAttribute("order", order.get());
                model.addAttribute("orderDetails", orderDetailsRepository.findAllByOrderId(id));
            } else {
                throw new EntityNotFoundException();
            }
        } else {
            model.addAttribute("order", new SalesOrder());
        }

        return "fragments/order_form";
    }


    
    @GetMapping("/order/details/{id}")
    @ResponseBody
    public Map<String, Object> getOrderDetails(@PathVariable Long id) {
        Optional<SalesOrder> orderOpt = orderRepository.findById(id);
        if (!orderOpt.isPresent()) throw new EntityNotFoundException("Order not found");

        SalesOrder order = orderOpt.get();
        List<OrderDetails> details = orderDetailsRepository.findAllByOrderId(order.getId());

        List<Map<String, Object>> items = details.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productName", d.getProduct().getName());
            map.put("quantity", d.getQuantity());
            map.put("price", d.getUnitPrice());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("customerName", order.getCustomer().getName());
        response.put("customerPhone", order.getCustomer().getPhoneNumber());
        response.put("customerAddress", order.getCustomer().getAddress());
        response.put("invoiceNumber", order.getInvoiceNumber());
        response.put("orderDate", order.getDateOrdered() != null ? order.getDateOrdered().toString() : "");
        response.put("amountPaid", order.getAmountPaid() != null ? order.getAmountPaid() : BigDecimal.ZERO);
        response.put("amountDue", order.getAmountDue() != null ? order.getAmountDue() : BigDecimal.ZERO);
        response.put("items", items);

        return response;
    }



    @Transactional
    @PostMapping(value = { "/order" })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String createUpdateOrder(@Valid SalesOrder order, HttpServletRequest request, Errors errors, Model model) {

        if (errors != null && errors.getErrorCount() > 0) {
            return "fragments/order_form";
        }

        String[] productIds = request.getParameterValues("order_detail_product");
        String[] quantities = request.getParameterValues("order_detail_qty");
        String[] prices = request.getParameterValues("order_detail_price");

        List<OrderDetails> existingOrderDetailsList = orderDetailsRepository.findAllByOrderId(order.getId());
        List<OrderDetails> newOrderDetailsList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (productIds != null && productIds.length > 0 && quantities != null && quantities.length > 0) {

            for (int i = 0; i < productIds.length; i++) {
                final int index = i;  // capture i for lambda

                Product product = productRepository.findById(Long.parseLong(productIds[index]))
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + productIds[index]));

                int quantity = Integer.parseInt(quantities[index]);

                // default price
                BigDecimal price = product.getSellingPrice();

                if (prices != null && index < prices.length && prices[index] != null && !prices[index].isEmpty()) {
                    price = new BigDecimal(prices[index]).setScale(2, RoundingMode.HALF_UP);
                }

                // Create order details
                OrderDetails orderDetails = new OrderDetails();
                orderDetails.setProduct(product);
                orderDetails.setUnitPrice(price);
                orderDetails.setQuantity(quantity);
                orderDetails.setOrder(order);

                newOrderDetailsList.add(orderDetails);

                // totalAmount += quantity * price
                totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(quantity)));
            }
        }

        // convert BigDecimal to double for Order entity
        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));

        // save order first
        order = orderRepository.save(order);

        // delete existing order details
        orderDetailsRepository.deleteAllByOrderId(order.getId());

        // save new order details
        orderDetailsRepository.saveAll(newOrderDetailsList);

        // update product quantities
        updateProductQuantities(existingOrderDetailsList, newOrderDetailsList);

        return "redirect:/orders";
    }



    @Transactional
    @PostMapping("/orders/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteOrder(@RequestParam("id") Long id) {

        List<OrderDetails> existingOrderDetailsList = orderDetailsRepository.findAllByOrderId(id);
        orderDetailsRepository.deleteAllByOrderId(id);
        orderRepository.deleteById(id);
        // update product quantities
        updateProductQuantities(existingOrderDetailsList, null);

        return "redirect:/orders";
    }

    private void updateProductQuantities(List<OrderDetails> existingOrderDetailsList,
            List<OrderDetails> newOrderDetailsList) {

        Map<Long, Integer> productQuantities = new HashMap<Long, Integer>();

        if (!CollectionUtils.isEmpty(existingOrderDetailsList)) {
            for (OrderDetails detail : existingOrderDetailsList) {
                int qty = productQuantities.getOrDefault(detail.getProduct().getId(), 0);

                qty += detail.getQuantity();
                productQuantities.put(detail.getProduct().getId(), qty);
            }
        }

        if (!CollectionUtils.isEmpty(newOrderDetailsList)) {
            for (OrderDetails detail : newOrderDetailsList) {
                int qty = productQuantities.getOrDefault(detail.getProduct().getId(), 0);

                qty -= detail.getQuantity();
                productQuantities.put(detail.getProduct().getId(), qty);
            }
        }

        productQuantities.entrySet().forEach(entry -> {

            productRepository.updateQuantity(entry.getKey(), entry.getValue());
        });

    }

}