package com.business.salesync.controller;

import com.business.salesync.repository.CustomerRepository;
import com.business.salesync.repository.ProductRepository;
import com.business.salesync.service.ReportService;
import java.time.LocalDate;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ReportController serves as the thin web layer for generating various business reports.
 * It delegates all report generation logic and complex calculations to the ReportService.
 */
@Controller
@RequestMapping("/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    // Dependencies retained for simple lookups (Stock/Customer)
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    // Service for complex calculations and data aggregation
    private final ReportService reportService;

    /**
     * Uses Constructor Injection for all required dependencies (best practice).
     */
    public ReportController(
        ProductRepository productRepository,
        CustomerRepository customerRepository,
        ReportService reportService
    ) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.reportService = reportService;
    }

    /**
     * Displays the main report selection page.
     */
    @GetMapping
    public String reportsPage() {
        logger.info("Reports page requested");
        return "fragments/reports";
    }

    /**
     * Generates and displays the requested report type based on date range.
     */
    @GetMapping("/view")
    public String generateReport(
        @RequestParam("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
        @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) LocalDate to,
        @RequestParam("type") String type,
        Model model
    ) {
        logger.info("Generate Report called: from={}, to={}, type={}", from, to, type);

        switch (type.toLowerCase()) {
            case "stock":
                model.addAttribute("reportTitle", "Stock Report");
                // Simple case: directly fetch all products
                model.addAttribute("products", this.productRepository.findAll());
                return "fragments/report_view";

            case "sales":
                model.addAttribute("reportTitle", "Sales Report");
                // Complex case: delegate calculation to service
                Map<String, Object> salesData = this.reportService.generateSalesReport(from, to);
                model.addAllAttributes(salesData);
                return "fragments/reports_sale";

            case "profit-loss":
                model.addAttribute("reportTitle", "Profit and Loss Report");
                // Complex case: delegate calculation to service
                Map<String, Object> profitLossData = this.reportService.generateProfitLossReport(from, to);
                model.addAllAttributes(profitLossData);
                return "fragments/profit-loss";

            case "customer":
                model.addAttribute("reportTitle", "Customer Report");
                // Simple case: directly fetch all customers
                model.addAttribute("customers", this.customerRepository.findAll());
                return "fragments/report_view";

            default:
                model.addAttribute("reportTitle", "Unknown Report");
                return "fragments/report_view";
        }
    }
}