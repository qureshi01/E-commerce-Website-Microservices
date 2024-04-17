package com.ecommerce.product.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ecommerce.product.dto.CommonApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CategorySaveFailedException.class)
	public ResponseEntity<CommonApiResponse> handleCategorySaveFailedException(CategorySaveFailedException ex) {
		String responseMessage = ex.getMessage();

		CommonApiResponse apiResponse = new CommonApiResponse();
		apiResponse.setResponseMessage(responseMessage);
		apiResponse.setSuccess(false);
		return new ResponseEntity<CommonApiResponse>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);

	}

}
