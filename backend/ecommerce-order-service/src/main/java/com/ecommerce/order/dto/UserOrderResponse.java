package com.ecommerce.order.dto;

import java.util.ArrayList;
import java.util.List;

public class UserOrderResponse extends CommonApiResponse {

	List<MyOrderResponse> orders = new ArrayList<>();

	public List<MyOrderResponse> getOrders() {
		return orders;
	}

	public void setOrders(List<MyOrderResponse> orders) {
		this.orders = orders;
	}

}
