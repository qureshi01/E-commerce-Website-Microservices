package com.ecommerce.user.dto;

import java.util.ArrayList;
import java.util.List;

import com.ecommerce.user.model.User;

public class UserResponse extends CommonApiResponse {

	private List<User> users = new ArrayList<>();

	private String jwtToken;

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
	}

}
