package com.ecommerce.order.dto;

import java.util.ArrayList;
import java.util.List;

public class CartResponse extends CommonApiResponse {
	
	private String totalCartPrice;
	
	private List<CartDataResponse> cartData = new ArrayList();

	public String getTotalCartPrice() {
		return totalCartPrice;
	}

	public void setTotalCartPrice(String totalCartPrice) {
		this.totalCartPrice = totalCartPrice;
	}

	public List<CartDataResponse> getCartData() {
		return cartData;
	}

	public void setCartData(List<CartDataResponse> cartData) {
		this.cartData = cartData;
	}

	@Override
	public String toString() {
		return "CartResponse [totalCartPrice=" + totalCartPrice + ", cartData=" + cartData + "]";
	}

}
