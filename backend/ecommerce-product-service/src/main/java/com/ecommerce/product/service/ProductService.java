package com.ecommerce.product.service;

import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.product.model.Product;

public interface ProductService {
	
	void addProduct(Product product, MultipartFile productImmage);

}
