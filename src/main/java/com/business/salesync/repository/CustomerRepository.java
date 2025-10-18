package com.business.salesync.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.business.salesync.models.Customer;


public interface CustomerRepository extends JpaRepository<Customer, Long> {

	public List<Customer> findByName(String name);

}
