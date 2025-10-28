package com.business.salesync.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.SupplierLedger;

@Repository
public interface SupplierLedgerRepository extends JpaRepository<SupplierLedger, Long> {
    List<SupplierLedger> findBySupplierIdOrderByTrnDateAsc(Long supplierId);
}