package com.business.salesync.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.business.salesync.models.Product;
import com.business.salesync.repository.ProductRepository;

@RestController
@RequestMapping("/api")
public class ProductApiController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productRepository.findAll(); // Returns JSON
    }
}

