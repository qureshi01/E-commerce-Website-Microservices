package com.ecommerce.cart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ecommerce.cart.dto.CommonApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CartSaveFailedException.class)
	public ResponseEntity<CommonApiResponse> handleCartSaveFailedException(CartSaveFailedException ex) {
		String responseMessage = ex.getMessage();

		CommonApiResponse apiResponse = new CommonApiResponse();
		apiResponse.setResponseMessage(responseMessage);
		apiResponse.setSuccess(false);
		return new ResponseEntity<CommonApiResponse>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@ExceptionHandler(ServiceDownException.class)
	public ResponseEntity<CommonApiResponse> handleServiceDownException(ServiceDownException ex) {
		String responseMessage = ex.getMessage();

		CommonApiResponse apiResponse = new CommonApiResponse();
		apiResponse.setResponseMessage(responseMessage);
		apiResponse.setSuccess(false);
		return new ResponseEntity<CommonApiResponse>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);

	}

}
