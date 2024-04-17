package com.ecommerce.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.CommonApiResponse;
import com.ecommerce.cart.resource.CartResource;

@RestController
@RequestMapping("api/cart")
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

	@Autowired
	private CartResource cartResource;

	@PostMapping("add")
	public ResponseEntity<CommonApiResponse> add(@RequestBody AddToCartRequest addToCartRequest) {
		return this.cartResource.add(addToCartRequest);
	}

	@GetMapping("fetch")
	public ResponseEntity<CartResponse> getMyCart(@RequestParam("userId") int userId) throws JsonProcessingException {
		return this.cartResource.getMyCart(userId);
	}

	@GetMapping("remove")
	public ResponseEntity<CommonApiResponse> removeCartItem(@RequestParam("cartId") int cartId)
			throws JsonProcessingException {

		return this.cartResource.removeCartItem(cartId);
	}

}
