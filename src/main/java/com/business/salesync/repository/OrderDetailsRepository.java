package com.business.salesync.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.business.salesync.models.OrderDetails;


public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Long> {

	List<OrderDetails> findAllByOrderId(Long orderId);

	void deleteAllByOrderId(Long orderId);

}
