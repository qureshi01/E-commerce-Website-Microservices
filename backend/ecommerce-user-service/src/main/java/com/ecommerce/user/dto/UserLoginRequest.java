package com.ecommerce.user.dto;

public class UserLoginRequest {

	private String emailId;
	private String password;
	private String role;

	private String newPassword;  // for forget password
	
	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	@Override
	public String toString() {
		return "UserLoginRequest [emailId=" + emailId + ", password=" + password + "]";
	}

	public static boolean validateLoginRequest(UserLoginRequest request) {
		if (request.getEmailId() == null || request.getPassword() == null || request.getRole() == null) {
			return false;
		}

		return true;
	}
	
	public static boolean validateForgetRequest(UserLoginRequest request) {
		if (request.getEmailId() == null || request.getPassword() == null || request.getNewPassword() == null) {
			return false;
		}

		return true;
	}
}
