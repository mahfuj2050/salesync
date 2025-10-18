package com.business.salesync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.business.salesync.models.Category;



public interface CategoryRepository extends JpaRepository<Category, Long> {
	
}
