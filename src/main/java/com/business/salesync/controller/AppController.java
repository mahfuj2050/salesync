package com.business.salesync.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.business.salesync.repository.CustomerRepository;
import com.business.salesync.repository.OrderRepository;
import com.business.salesync.repository.ProductRepository;



@Controller
public class AppController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/")
    public String home(Model model) {

        long productCount = productRepository.count();
        long customerCount = customerRepository.count();
        long orderCount = orderRepository.count();

        model.addAttribute("product_count", productCount);
        model.addAttribute("customer_count", customerCount);
        model.addAttribute("order_count", orderCount);

        // Today
        LocalDate today = LocalDate.now();
        double salesToday = orderRepository.totalSalesToday(today);
        String formattedSalesToday = String.format("%.2f", salesToday);
        model.addAttribute("sales_today", formattedSalesToday);

        // This week (Monday to Sunday)
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);
        double salesThisWeek = orderRepository.totalSalesThisWeek(startOfWeek, endOfWeek);
        String formattedSalesThisWeek = String.format("%.2f", salesToday);
        model.addAttribute("sales_week", formattedSalesThisWeek);

        // This month
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        double salesThisMonth = orderRepository.totalSalesThisMonth(startOfMonth, endOfMonth);
        String formattedSalesThisMonth = String.format("%.2f", salesToday);
        model.addAttribute("sales_month", formattedSalesThisMonth);

        return "fragments/home";
    }


}