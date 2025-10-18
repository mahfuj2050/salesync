package com.business.salesync.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.business.salesync.models.OrderDetails;
import com.business.salesync.models.SalesOrder;
import com.business.salesync.repository.CustomerRepository;
import com.business.salesync.repository.OrderRepository;
import com.business.salesync.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    public String reportsPage() {
        logger.info("Reports page requested");
        return "fragments/reports";
    }

    @GetMapping("/view")
    public String generateReport(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam("type") String type,
            Model model) {

        logger.info("Generate Report called: from={}, to={}, type={}", from, to, type);

        switch (type.toLowerCase()) {
            case "stock":
                model.addAttribute("reportTitle", "Stock Report");
                model.addAttribute("products", productRepository.findAll());
                return "fragments/report_view";   // Stock template

            case "sales":
                model.addAttribute("reportTitle", "Sales Report");

                // Get orders in date range
                List<SalesOrder> orders = orderRepository.findByDateOrderedBetween(from, to);

                // Flatten order details
                List<OrderDetails> orderDetails = orders.stream()
                        .flatMap(o -> o.getOrderDetails().stream())
                        .collect(Collectors.toList());

                // Summary calculations
                int totalQuantity = orderDetails.stream()
                        .mapToInt(OrderDetails::getQuantity)
                        .sum();

                BigDecimal totalSales = orderDetails.stream()
                        .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalProfit = orderDetails.stream()
                        .map(d -> d.getUnitPrice()
                                .subtract(d.getProduct().getCostPrice())
                                .multiply(BigDecimal.valueOf(d.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Add attributes to model
                model.addAttribute("orderDetails", orderDetails);
                model.addAttribute("totalQuantity", totalQuantity);
                model.addAttribute("totalSales", totalSales);
                model.addAttribute("totalProfit", totalProfit);
                model.addAttribute("reportDate", LocalDate.now());

                return "fragments/reports_sale";   // Sales template

            case "customer":
                model.addAttribute("reportTitle", "Customer Report");
                model.addAttribute("customers", customerRepository.findAll());
                return "fragments/report_view";   // Customer template

            default:
                model.addAttribute("reportTitle", "Unknown Report");
                return "fragments/report_view";
        }
    }
    
    


}

