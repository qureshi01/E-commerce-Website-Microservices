package com.ecommerce.order.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.order.model.Orders;

@Repository
public interface OrderDao extends JpaRepository<Orders, Integer> {

	List<Orders> findByOrderId(String orderId);
	List<Orders> findByUserIdAndProductId(int userId, int productId);
	List<Orders> findByUserId(int userId);
	List<Orders> findByDeliveryPersonId(int deliveryPersonId);
	 
}
