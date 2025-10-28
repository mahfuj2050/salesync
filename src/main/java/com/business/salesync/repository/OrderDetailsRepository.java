package com.business.salesync.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.business.salesync.models.OrderDetails;


public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Long> {
	
	 // Find all SaleOrderDetails linked to a specific Product
    List<OrderDetails> findByProductId(Long productId);

	List<OrderDetails> findAllByOrderId(Long orderId);

	void deleteAllByOrderId(Long orderId);

}
