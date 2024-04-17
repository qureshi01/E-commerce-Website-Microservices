package com.ecommerce.order.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.order.dto.CartResponse;
import com.ecommerce.order.dto.CommonApiResponse;

@Component
@FeignClient(name = "ecommerce-cart-service")
public interface CartService {

	@GetMapping("/api/cart/fetch")
	CartResponse getCartByUserId(@RequestParam("userId") int userId);
	
	@GetMapping("/api/cart/remove")
	CommonApiResponse removeCartById(@RequestParam("cartId") int cartId);

}
