package com.business.salesync.service;

import com.business.salesync.models.OrderDetails;
import com.business.salesync.models.SalesOrder;
import com.business.salesync.repository.OrderDetailsRepository;
import com.business.salesync.repository.OrderRepository;
import com.business.salesync.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * ReportService handles all business logic, data aggregation, and calculations
 * for generating various financial and stock reports.
 */
@Service
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductRepository productRepository;

    // Constructor Injection (best practice)
    public ReportService(
        OrderRepository orderRepository,
        OrderDetailsRepository orderDetailsRepository,
        ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.productRepository = productRepository;
    }

    /**
     * Generates a Sales Report summary for a given date range.
     * @param from Start date
     * @param to End date
     * @return A map containing aggregated sales data and detailed order lines.
     */
    public Map<String, Object> generateSalesReport(LocalDate from, LocalDate to) {
        // 1. Fetch Sales Orders within the date range
        List<SalesOrder> orders = this.orderRepository.findByDateOrderedBetween(from, to);

        // 2. Flatten and Collect all OrderDetails from the fetched orders
        List<OrderDetails> orderDetails = orders.stream()
            .flatMap(o -> o.getOrderDetails().stream())
            .collect(Collectors.toList());

        // 3. Calculate Summary Statistics
        int totalQuantity = orderDetails.stream()
            .mapToInt(OrderDetails::getQuantity)
            .sum();

        // Calculate Total Sales (sum of UnitPrice * Quantity)
        BigDecimal totalSales = orderDetails.stream()
            .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate Total Profit (sum of (UnitPrice - CostPrice) * Quantity)
        BigDecimal totalProfit = orderDetails.stream()
            .map(d -> d.getUnitPrice()
                .subtract(d.getProduct().getCostPrice())
                .multiply(BigDecimal.valueOf(d.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Prepare results map for the controller
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("orderDetails", orderDetails);
        reportData.put("totalQuantity", totalQuantity);
        reportData.put("totalSales", totalSales);
        reportData.put("totalProfit", totalProfit);
        reportData.put("reportDate", LocalDate.now());

        return reportData;
    }

    /**
     * Generates a detailed Profit and Loss Report for a given date range.
     * Logic relies on fetching SalesOrders and then flattening their OrderDetails
     * for unified and robust data access.
     * @param from Start date
     * @param to End date
     * @return A map containing P&L aggregates and the detailed line item list.
     */
    public Map<String, Object> generateProfitLossReport(LocalDate from, LocalDate to) {
        // 1. Fetch Sales Orders within the date range (using reliable OrderRepository method)
        List<SalesOrder> orders = this.orderRepository.findByDateOrderedBetween(from, to);

        // 2. Flatten and Collect all OrderDetails
        List<OrderDetails> details = orders.stream()
            .flatMap(o -> o.getOrderDetails().stream())
            .collect(Collectors.toList());
            
        List<Map<String, Object>> detailList = new ArrayList<>();
        BigDecimal totalPurchase = BigDecimal.ZERO;
        BigDecimal totalSelling = BigDecimal.ZERO;
        BigDecimal totalProfitPL = BigDecimal.ZERO;

        for (OrderDetails od : details) {
            // Safety check for product data integrity
            if (od.getProduct() == null) continue;

            // Calculations use CostPrice from the Product entity
            BigDecimal purchase = od.getProduct().getCostPrice().multiply(BigDecimal.valueOf(od.getQuantity()));
            BigDecimal selling = od.getUnitPrice().multiply(BigDecimal.valueOf(od.getQuantity()));
            BigDecimal profit = selling.subtract(purchase);

            totalPurchase = totalPurchase.add(purchase);
            totalSelling = totalSelling.add(selling);
            totalProfitPL = totalProfitPL.add(profit);

            Map<String, Object> row = new HashMap<>();
            
            // Extract invoice details from the associated SalesOrder
            if (od.getOrder() != null) {
                row.put("invoiceNumber", od.getOrder().getInvoiceNumber());
                row.put("invoiceDate", od.getOrder().getDateOrdered());
            } else {
                 row.put("invoiceNumber", "N/A");
                 row.put("invoiceDate", "N/A");
            }
            
            row.put("productName", od.getProduct().getName());
            row.put("soldQty", od.getQuantity());
            row.put("purchasePrice", purchase);
            row.put("sellingPrice", selling);
            row.put("profitAmount", profit);
            detailList.add(row);
        }

        // Prepare results map for the controller
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("details", detailList);
        reportData.put("totalPurchase", totalPurchase);
        reportData.put("totalSelling", totalSelling);
        reportData.put("totalProfit", totalProfitPL);
        reportData.put("fromDate", from);
        reportData.put("toDate", to);

        return reportData;
    }
}