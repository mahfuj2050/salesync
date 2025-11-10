package com.business.salesync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.business.salesync.models.SalesReturnItem;

@Repository
public interface SalesReturnItemRepository extends JpaRepository<SalesReturnItem, Long> { 
	
	
}
