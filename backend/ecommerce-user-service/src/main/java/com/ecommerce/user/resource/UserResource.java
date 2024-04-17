package com.ecommerce.user.resource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ecommerce.user.config.CustomUserDetailsService;
import com.ecommerce.user.dao.AddressDao;
import com.ecommerce.user.dao.UserDao;
import com.ecommerce.user.dto.AddUserRequest;
import com.ecommerce.user.dto.UserLoginRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.exception.UserSaveFailedException;
import com.ecommerce.user.model.Address;
import com.ecommerce.user.model.User;
import com.ecommerce.user.utility.JwtUtil;

@Component
@Transactional
public class UserResource {

	@Autowired
	private UserDao userDao;

	@Autowired
	private AddressDao addressDao;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private JwtUtil jwtUtil;

	public ResponseEntity<UserResponse> registerUser(AddUserRequest userRequest) {
		UserResponse response = new UserResponse();

		if (userRequest == null) {
			response.setResponseMessage("bad request - missing request");
			response.setSuccess(false);

			return new ResponseEntity<UserResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (!AddUserRequest.validate(userRequest)) {
			response.setResponseMessage("bad request - missing input");
			response.setSuccess(false);

			return new ResponseEntity<UserResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (userRequest.getPhoneNo().length() != 10) {
			response.setResponseMessage("Enter Valid Mobile No");
			response.setSuccess(false);

			return new ResponseEntity<UserResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Address address = new Address();
		address.setCity(userRequest.getCity());
		address.setPincode(userRequest.getPincode());
		address.setStreet(userRequest.getStreet());

		Address addAddress = addressDao.save(address);

		if (addAddress == null) {
			response.setResponseMessage("Failed to register User");
			response.setSuccess(false);

			return new ResponseEntity<UserResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		String encodedPassword = passwordEncoder.encode(userRequest.getPassword());

		User user = new User();
		user.setAddress(addAddress);
		user.setEmailId(userRequest.getEmailId());
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());
		user.setPhoneNo(userRequest.getPhoneNo());
		user.setPassword(encodedPassword);
		user.setRole(userRequest.getRole());
		User addUser = userDao.save(user);

		if (addUser == null) {
			throw new UserSaveFailedException("Failed to register the User");
		}

		response.setUsers(Arrays.asList(addUser));
		response.setResponseMessage("User Registered Successful");
		response.setSuccess(true);

		return new ResponseEntity<UserResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserResponse> loginUser(UserLoginRequest loginRequest) {
		UserResponse response = new UserResponse();

		if (loginRequest == null) {
			response.setResponseMessage("bad request - missing request");
			response.setSuccess(false);

			return new ResponseEntity<UserResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (!UserLoginRequest.validateLoginRequest(loginRequest)) {
			response.setResponseMessage("bad request - missing input");
			response.setSuccess(false);

			return new ResponseEntity<UserResponse>(response, HttpStatus.BAD_REQUEST);
		}

		User user = null;
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getEmailId(), loginRequest.getPassword()));
		} catch (Exception ex) {
			response.setSuccess(false);
			response.setResponseMessage("Invalid Login Credentials!!!");
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}

		UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginRequest.getEmailId());

		user = userDao.findByEmailId(loginRequest.getEmailId());
		
		if(!user.getRole().equals(loginRequest.getRole())) {
			response.setSuccess(false);
			response.setResponseMessage("Failed to Login!!!");
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		
		}
		
		String jwtToken = null;
		
		for (GrantedAuthority grantedAuthory : userDetails.getAuthorities()) {
			if (grantedAuthory.getAuthority().equals(loginRequest.getRole())) {
				jwtToken = jwtUtil.generateToken(userDetails.getUsername());
			}
		}

		// user is authenticated
		if (jwtToken != null) {		
			response.setSuccess(true);
			response.setResponseMessage("Logged in Successful");
			response.setJwtToken(jwtToken);
			response.setUsers(Arrays.asList(user));
			return new ResponseEntity(response, HttpStatus.OK);
		}

		else {
			response.setSuccess(false);
			response.setResponseMessage("Failed to Login!!!");
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<UserResponse> getAllDeliveryPersons() {
		UserResponse response = new UserResponse();

		List<User> deliveryPersons = this.userDao.findByRole("Delivery");

		if (CollectionUtils.isEmpty(deliveryPersons)) {
			response.setResponseMessage("No Delivery Person Found");
			response.setSuccess(false);

			return new ResponseEntity<UserResponse>(response, HttpStatus.OK);
		}

		response.setUsers(deliveryPersons);
		response.setResponseMessage("Delivery Persons Fected Success!!!");
		response.setSuccess(true);

		return new ResponseEntity<UserResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserResponse> fetchUserById(Integer userId) {
		UserResponse response = new UserResponse();

		if (userId == null) {
			response.setResponseMessage("User Id is missing!!");
			response.setSuccess(false);

			return new ResponseEntity<UserResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<User> optional = this.userDao.findById(userId);

		if (optional.isEmpty()) {
			response.setResponseMessage("User not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<UserResponse>(response, HttpStatus.BAD_REQUEST);
		}

		User user = optional.get();

		response.setUsers(Arrays.asList(user));
		response.setResponseMessage("User Fetched Successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<UserResponse>(response, HttpStatus.OK);
	}

}
