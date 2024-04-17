package com.ecommerce.order.resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.management.ServiceNotFoundException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ecommerce.order.dao.OrderDao;
import com.ecommerce.order.dto.CartDataResponse;
import com.ecommerce.order.dto.CartResponse;
import com.ecommerce.order.dto.CommonApiResponse;
import com.ecommerce.order.dto.MyOrderResponse;
import com.ecommerce.order.dto.Product;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UpdateDeliveryStatusRequest;
import com.ecommerce.order.dto.User;
import com.ecommerce.order.dto.UserOrderResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.exception.OrderSaveFailedException;
import com.ecommerce.order.external.CartService;
import com.ecommerce.order.external.ProductService;
import com.ecommerce.order.external.UserService;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.utility.Constants.DeliveryStatus;
import com.ecommerce.order.utility.Constants.DeliveryTime;
import com.ecommerce.order.utility.Constants.IsDeliveryAssigned;
import com.ecommerce.order.utility.Helper;

@Component
@Transactional
public class OrderResource {

	@Autowired
	private OrderDao orderDao;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductService productService;

	public ResponseEntity<CommonApiResponse> customerOrder(int userId) throws ServiceNotFoundException {
		CommonApiResponse response = new CommonApiResponse();

		if (userId == 0) {
			response.setResponseMessage("bad request - missing field");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		String orderId = Helper.getAlphaNumericOrderId();

		CartResponse cartResponse = this.cartService.getCartByUserId(userId);

		if (cartResponse == null) {
			throw new ServiceNotFoundException("Cart Service is down!!!");
		}

		List<CartDataResponse> carts = cartResponse.getCartData();

		if (CollectionUtils.isEmpty(carts)) {
			response.setResponseMessage("Your Cart is Empty!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
		}

		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		String formatDateTime = currentDateTime.format(formatter);

		try {

			for (CartDataResponse cart : carts) {

				Orders order = new Orders();
				order.setOrderId(orderId);
				order.setUserId(userId);
				order.setProductId(cart.getProductId());
				order.setQuantity(cart.getQuantity());
				order.setOrderDate(formatDateTime);
				order.setDeliveryDate(DeliveryStatus.PENDING.value());
				order.setDeliveryStatus(DeliveryStatus.PENDING.value());
				order.setDeliveryTime(DeliveryTime.DEFAULT.value());
				order.setDeliveryAssigned(IsDeliveryAssigned.NO.value());

				CommonApiResponse cartRemoveResponse = cartService.removeCartById(cart.getCartId());

				Orders savedOrder = orderDao.save(order);

				if (savedOrder == null) {
					throw new OrderSaveFailedException("Failed to save the Order");
				}
			}

		} catch (Exception e) {
			response.setResponseMessage("Failed to Order Products!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.setResponseMessage("Your Order Placed, Order Id: " + orderId);
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> getMyOrder(int userId) {
		UserOrderResponse response = new UserOrderResponse();

		if (userId == 0) {
			response.setResponseMessage("User Id missing");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<Orders> userOrder = orderDao.findByUserId(userId);

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		if (CollectionUtils.isEmpty(userOrder)) {
			response.setResponseMessage("Orders not found");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
		}

		for (Orders order : userOrder) {

			ProductResponse productResponse = productService.getProductId(order.getProductId());

			if (productResponse == null) {
				response.setResponseMessage("product service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			Product product = productResponse.getProducts().get(0);
			
			System.out.println(product);

			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(product.getDescription());
			orderData.setProductName(product.getTitle());
			orderData.setProductImage(product.getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(product.getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(product.getPrice().toString())));
			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {

				User deliveryPerson = null;

				UserResponse userResponse = this.userService.getUserById(order.getDeliveryPersonId());

				if (userResponse == null) {
					response.setResponseMessage("user service is down!!!");
					response.setSuccess(false);

					return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
				}

				deliveryPerson = userResponse.getUsers().get(0);

				if (deliveryPerson != null) {
					orderData.setDeliveryPersonContact(deliveryPerson.getPhoneNo());
					orderData.setDeliveryPersonName(deliveryPerson.getFirstName());
				}

			}
			orderDatas.add(orderData);
		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> getAllOrder() {
		UserOrderResponse response = new UserOrderResponse();

		List<Orders> userOrder = orderDao.findAll();

		if (CollectionUtils.isEmpty(userOrder)) {
			response.setResponseMessage("Orders not found");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
		}

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		for (Orders order : userOrder) {

			ProductResponse productResponse = productService.getProductId(order.getProductId());

			if (productResponse == null) {
				response.setResponseMessage("product service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			Product product = productResponse.getProducts().get(0);

			User user = null;

			UserResponse userResponse = this.userService.getUserById(order.getUserId());

			if (userResponse == null) {
				response.setResponseMessage("user service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			user = userResponse.getUsers().get(0);

			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(product.getDescription());
			orderData.setProductName(product.getTitle());
			orderData.setProductImage(product.getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(product.getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(product.getPrice().toString())));
			orderData.setUserId(user.getId());
			orderData.setUserName(user.getFirstName() + " " + user.getLastName());
			orderData.setUserPhone(user.getPhoneNo());
			orderData.setAddress(user.getAddress());
			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {
				User deliveryPerson = null;

				UserResponse deliveryPersonResponse = this.userService.getUserById(order.getDeliveryPersonId());

				if (deliveryPersonResponse == null) {
					response.setResponseMessage("user service is down!!!");
					response.setSuccess(false);

					return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
				}

				deliveryPerson = deliveryPersonResponse.getUsers().get(0);

				orderData.setDeliveryPersonContact(deliveryPerson.getPhoneNo());
				orderData.setDeliveryPersonName(deliveryPerson.getFirstName());
			}
			orderDatas.add(orderData);

		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> getOrdersByOrderId(String orderId) {
		UserOrderResponse response = new UserOrderResponse();

		if (orderId == null) {
			response.setResponseMessage("Orders not found");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
		}

		List<Orders> userOrder = orderDao.findByOrderId(orderId);

		if (CollectionUtils.isEmpty(userOrder)) {
			response.setResponseMessage("Orders not found");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
		}

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		for (Orders order : userOrder) {

			ProductResponse productResponse = productService.getProductId(order.getProductId());

			if (productResponse == null) {
				response.setResponseMessage("product service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			Product product = productResponse.getProducts().get(0);

			User user = null;

			UserResponse userResponse = this.userService.getUserById(order.getUserId());

			if (userResponse == null) {
				response.setResponseMessage("user service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			user = userResponse.getUsers().get(0);

			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(product.getDescription());
			orderData.setProductName(product.getTitle());
			orderData.setProductImage(product.getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(product.getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(product.getPrice().toString())));
			orderData.setUserId(user.getId());
			orderData.setUserName(user.getFirstName() + " " + user.getLastName());
			orderData.setUserPhone(user.getPhoneNo());
			orderData.setAddress(user.getAddress());
			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {
				User deliveryPerson = null;

				UserResponse deliveryPersonResponse = this.userService.getUserById(order.getDeliveryPersonId());

				if (deliveryPersonResponse == null) {
					response.setResponseMessage("user service is down!!!");
					response.setSuccess(false);

					return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
				}

				deliveryPerson = deliveryPersonResponse.getUsers().get(0);

				orderData.setDeliveryPersonContact(deliveryPerson.getPhoneNo());
				orderData.setDeliveryPersonName(deliveryPerson.getFirstName());
			}
			orderDatas.add(orderData);

		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> updateOrderDeliveryStatus(UpdateDeliveryStatusRequest deliveryRequest) {
		UserOrderResponse response = new UserOrderResponse();

		if (deliveryRequest == null) {
			response.setResponseMessage("bad request - missing request");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<Orders> orders = orderDao.findByOrderId(deliveryRequest.getOrderId());

		if (CollectionUtils.isEmpty(orders)) {
			response.setResponseMessage("Orders not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		for (Orders order : orders) {
			order.setDeliveryDate(deliveryRequest.getDeliveryDate());
			order.setDeliveryStatus(deliveryRequest.getDeliveryStatus());
			order.setDeliveryTime(deliveryRequest.getDeliveryTime());
			orderDao.save(order);
		}

		List<Orders> userOrder = orderDao.findByOrderId(deliveryRequest.getOrderId());

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		for (Orders order : userOrder) {

			ProductResponse productResponse = productService.getProductId(order.getProductId());

			if (productResponse == null) {
				response.setResponseMessage("product service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			Product product = productResponse.getProducts().get(0);

			User user = null;

			UserResponse userResponse = this.userService.getUserById(order.getUserId());

			if (userResponse == null) {
				response.setResponseMessage("user service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			user = userResponse.getUsers().get(0);

			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(product.getDescription());
			orderData.setProductName(product.getTitle());
			orderData.setProductImage(product.getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(product.getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(product.getPrice().toString())));
			orderData.setUserId(user.getId());
			orderData.setUserName(user.getFirstName() + " " + user.getLastName());
			orderData.setUserPhone(user.getPhoneNo());
			orderData.setAddress(user.getAddress());
			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {
				User deliveryPerson = null;

				UserResponse deliveryPersonResponse = this.userService.getUserById(order.getDeliveryPersonId());

				if (deliveryPersonResponse == null) {
					response.setResponseMessage("user service is down!!!");
					response.setSuccess(false);

					return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
				}

				deliveryPerson = deliveryPersonResponse.getUsers().get(0);

				orderData.setDeliveryPersonContact(deliveryPerson.getPhoneNo());
				orderData.setDeliveryPersonName(deliveryPerson.getFirstName());
			}
			orderDatas.add(orderData);

		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> assignDeliveryPersonForOrder(UpdateDeliveryStatusRequest deliveryRequest) {
		UserOrderResponse response = new UserOrderResponse();

		if (deliveryRequest == null) {
			response.setResponseMessage("bad request - missing request");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<Orders> orders = orderDao.findByOrderId(deliveryRequest.getOrderId());

		if (CollectionUtils.isEmpty(orders)) {
			response.setResponseMessage("Orders not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		for (Orders order : orders) {
			order.setDeliveryAssigned(IsDeliveryAssigned.YES.value());
			order.setDeliveryPersonId(deliveryRequest.getDeliveryId());
			orderDao.save(order);
		}

		List<Orders> userOrder = orderDao.findByOrderId(deliveryRequest.getOrderId());

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		for (Orders order : userOrder) {

			ProductResponse productResponse = productService.getProductId(order.getProductId());

			if (productResponse == null) {
				response.setResponseMessage("product service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			Product product = productResponse.getProducts().get(0);

			User user = null;

			UserResponse userResponse = this.userService.getUserById(order.getUserId());

			if (userResponse == null) {
				response.setResponseMessage("user service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			user = userResponse.getUsers().get(0);

			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(product.getDescription());
			orderData.setProductName(product.getTitle());
			orderData.setProductImage(product.getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(product.getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(product.getPrice().toString())));
			orderData.setUserId(user.getId());
			orderData.setUserName(user.getFirstName() + " " + user.getLastName());
			orderData.setUserPhone(user.getPhoneNo());
			orderData.setAddress(user.getAddress());

			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {
				User deliveryPerson = null;

				UserResponse deliveryPersonResponse = this.userService.getUserById(order.getDeliveryPersonId());

				if (deliveryPersonResponse == null) {
					response.setResponseMessage("user service is down!!!");
					response.setSuccess(false);

					return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
				}

				deliveryPerson = deliveryPersonResponse.getUsers().get(0);

				orderData.setDeliveryPersonContact(deliveryPerson.getPhoneNo());
				orderData.setDeliveryPersonName(deliveryPerson.getFirstName());
			}

			orderDatas.add(orderData);

		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> getMyDeliveryOrders(int deliveryPersonId) {
		UserOrderResponse response = new UserOrderResponse();

		if (deliveryPersonId == 0) {
			response.setResponseMessage("bad request - missing field");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		User deliveryPerson = null;

		UserResponse deliveryPersonResponse = this.userService.getUserById(deliveryPersonId);

		if (deliveryPersonResponse == null) {
			response.setResponseMessage("user service is down!!!");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		deliveryPerson = deliveryPersonResponse.getUsers().get(0);

		List<Orders> userOrder = orderDao.findByDeliveryPersonId(deliveryPersonId);

		if (CollectionUtils.isEmpty(userOrder)) {
			response.setResponseMessage("Orders not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		for (Orders order : userOrder) {

			ProductResponse productResponse = productService.getProductId(order.getProductId());

			if (productResponse == null) {
				response.setResponseMessage("product service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			Product product = productResponse.getProducts().get(0);

			User user = null;

			UserResponse userResponse = this.userService.getUserById(order.getUserId());

			if (userResponse == null) {
				response.setResponseMessage("user service is down!!!");
				response.setSuccess(false);

				return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
			}

			user = userResponse.getUsers().get(0);

			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(product.getDescription());
			orderData.setProductName(product.getTitle());
			orderData.setProductImage(product.getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(product.getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(product.getPrice().toString())));
			orderData.setUserId(user.getId());
			orderData.setUserName(user.getFirstName() + " " + user.getLastName());
			orderData.setUserPhone(user.getPhoneNo());
			orderData.setAddress(user.getAddress());

			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {
				orderData.setDeliveryPersonContact(deliveryPerson.getPhoneNo());
				orderData.setDeliveryPersonName(deliveryPerson.getFirstName());
			}

			orderDatas.add(orderData);

		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

}
