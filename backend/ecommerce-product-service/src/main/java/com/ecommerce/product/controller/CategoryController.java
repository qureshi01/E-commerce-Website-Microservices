package com.ecommerce.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.product.dto.CategoryResponse;
import com.ecommerce.product.dto.CommonApiResponse;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.resource.CategoryResource;

@RestController
@RequestMapping("api/product")
//@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

	@Autowired
	private CategoryResource categoryResource;

	@GetMapping("category/all")
	public ResponseEntity<CategoryResponse> getAllCategories() {
		return categoryResource.getAllCategories();
	}

	@PostMapping("category/add")
	public ResponseEntity<CommonApiResponse> add(@RequestBody Category category) {
		return categoryResource.add(category);
	}

}
