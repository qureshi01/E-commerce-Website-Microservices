package com.ecommerce.order.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.order.dto.UserResponse;

@Component
@FeignClient(name = "ecommerce-user-service")
public interface UserService {

	@GetMapping("/api/user/fetch")
	UserResponse getUserById(@RequestParam("userId") int userId);
	
}
