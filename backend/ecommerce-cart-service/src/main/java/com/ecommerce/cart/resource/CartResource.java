package com.ecommerce.cart.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ecommerce.cart.dao.CartDao;
import com.ecommerce.cart.dto.AddToCartRequest;
import com.ecommerce.cart.dto.CartDataResponse;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.CommonApiResponse;
import com.ecommerce.cart.dto.Product;
import com.ecommerce.cart.dto.ProductResponse;
import com.ecommerce.cart.dto.User;
import com.ecommerce.cart.dto.UserResponse;
import com.ecommerce.cart.exception.CartSaveFailedException;
import com.ecommerce.cart.external.ProductService;
import com.ecommerce.cart.external.UserService;
import com.ecommerce.cart.model.Cart;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component	
public class CartResource {

	@Autowired
	private CartDao cartDao;

	@Autowired
	private UserService userService;
	
	@Autowired
	private ProductService productService;
	
	ObjectMapper objectMapper = new ObjectMapper();

	public ResponseEntity<CommonApiResponse> add(AddToCartRequest addToCartRequest) {
		CommonApiResponse response = new CommonApiResponse();

		if (addToCartRequest == null) {
			response.setResponseMessage("bad request - missing request");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (!AddToCartRequest.validateAddToCartRequest(addToCartRequest)) {
			response.setResponseMessage("bad request - missing field");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Cart cart = new Cart();
		cart.setProductId(addToCartRequest.getProductId());
		cart.setQuantity(addToCartRequest.getQuantity());
		cart.setUserId(addToCartRequest.getUserId());

		Cart addedCart = cartDao.save(cart);

		if (addedCart == null) {
			throw new CartSaveFailedException("Failed to Save the Cart");
		}

		response.setResponseMessage("Cart added successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<CartResponse> getMyCart(int userId) {
		CartResponse response = new CartResponse();

		List<CartDataResponse> cartDatas = new ArrayList<>();

		if (userId == 0) {
			response.setResponseMessage("missing input user id");
			response.setSuccess(false);

			return new ResponseEntity<CartResponse>(response, HttpStatus.BAD_REQUEST);
		}
		
		UserResponse userResponse = this.userService.getUserById(userId);
		
		if(userResponse == null) {
			response.setResponseMessage("user service is down!!!");
			response.setSuccess(false);

			return new ResponseEntity<CartResponse>(response, HttpStatus.BAD_REQUEST);
		}

		User user = userResponse.getUsers().get(0);

		if (user == null) {
			response.setResponseMessage("user not found");
			response.setSuccess(false);

			return new ResponseEntity<CartResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<Cart> userCarts = cartDao.findByUserId(userId);

		if (CollectionUtils.isEmpty(userCarts)) {
			response.setResponseMessage("User carts not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<CartResponse>(response, HttpStatus.OK);
		}

		double totalCartPrice = 0;

		for (Cart cart : userCarts) {
			
			ProductResponse productResponse =  productService.getProductId(cart.getProductId());
			
			if(productResponse == null) {
				response.setResponseMessage("product service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<CartResponse>(response, HttpStatus.BAD_REQUEST);
			}
			
			Product product = productResponse.getProducts().get(0);
			
			CartDataResponse cartData = new CartDataResponse();
			cartData.setCartId(cart.getId());
			cartData.setProductDescription(product.getDescription());
			cartData.setProductName(product.getTitle());
			cartData.setProductImage(product.getImageName());
			cartData.setQuantity(cart.getQuantity());
			cartData.setProductId(product.getId());

			cartDatas.add(cartData);

			double productPrice = Double.parseDouble(product.getPrice().toString());

			totalCartPrice = totalCartPrice + (cart.getQuantity() * productPrice);

		}

		response.setTotalCartPrice(String.valueOf(totalCartPrice));
		response.setCartData(cartDatas);
		response.setResponseMessage("User cart fetched success!!!");
		response.setSuccess(true);

		return new ResponseEntity<CartResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<CommonApiResponse> removeCartItem(int cartId) {
		CommonApiResponse response = new CommonApiResponse();

		if (cartId == 0) {
			response.setResponseMessage("bad request - missing input");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<Cart> optionalCart = this.cartDao.findById(cartId);
		Cart cart = new Cart();

		if (optionalCart.isPresent()) {
			cart = optionalCart.get();
		}

		if (cart == null) {
			response.setResponseMessage("Cart not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			this.cartDao.delete(cart);
		} catch (Exception e) {
			response.setResponseMessage("Failed to delete Cart!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.setResponseMessage("product deleted from Cart Successfull!!!");
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
	}

}
