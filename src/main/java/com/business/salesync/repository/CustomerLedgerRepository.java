package com.business.salesync.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.CustomerLedger;

@Repository
public interface CustomerLedgerRepository extends JpaRepository<CustomerLedger, Long> {
    List<CustomerLedger> findByCustomerIdOrderByTrnDateAsc(Long customerId);
}