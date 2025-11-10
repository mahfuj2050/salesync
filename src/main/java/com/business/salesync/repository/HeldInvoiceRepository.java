package com.business.salesync.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.business.salesync.models.HeldInvoice;

public interface HeldInvoiceRepository extends JpaRepository<HeldInvoice, Long> {
	
    List<HeldInvoice> findByStatus(String status);

}
