package com.ecommerce.product.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import com.ecommerce.product.dao.CategoryDao;
import com.ecommerce.product.dao.ProductDao;
import com.ecommerce.product.dto.CommonApiResponse;
import com.ecommerce.product.dto.ProductAddRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.product.utility.StorageService;

@Component
public class ProductResource {

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductDao productDao;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private StorageService storageService;

	public ResponseEntity<CommonApiResponse> addProduct(ProductAddRequest productDto) {
		CommonApiResponse response = new CommonApiResponse();

		if (productDto == null) {
			response.setResponseMessage("bad request - missing request");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (!ProductAddRequest.validateProduct(productDto)) {
			response.setResponseMessage("bad request - missing field");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);

		}

		Product product = ProductAddRequest.toEntity(productDto);

		if (product == null) {
			response.setResponseMessage("bad request - missing field");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);

		}

		Optional<Category> optional = categoryDao.findById(productDto.getCategoryId());
		Category category = null;
		if (optional.isPresent()) {
			category = optional.get();
		}

		if (category == null) {
			response.setResponseMessage("please select correct product category");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);

		}

		product.setCategory(category);

		try {
			productService.addProduct(product, productDto.getImage());

			response.setResponseMessage("Product Added Successful!!!");
			response.setSuccess(true);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}

		response.setResponseMessage("Failed to add the Product");
		response.setSuccess(false);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public ResponseEntity<ProductResponse> getAllProducts() {
		ProductResponse response = new ProductResponse();

		List<Product> products = new ArrayList<Product>();

		products = productDao.findAll();

		if (CollectionUtils.isEmpty(products)) {
			response.setResponseMessage("Products not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.OK);
		}

		response.setProducts(products);
		response.setResponseMessage("Product Fetched Successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<ProductResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<ProductResponse> getProductById(int productId) {
		ProductResponse response = new ProductResponse();

		if (productId == 0) {
			response.setResponseMessage("Product Id is missing");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Product product = new Product();

		Optional<Product> optional = productDao.findById(productId);

		if (optional.isPresent()) {
			product = optional.get();
		}

		if (product == null) {
			response.setResponseMessage("Product not found");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.BAD_REQUEST);
		}

		response.setProducts(Arrays.asList(product));
		response.setResponseMessage("Product Fetched Successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<ProductResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<?> getProductsByCategories(int categoryId) {
		ProductResponse response = new ProductResponse();

		if (categoryId == 0) {
			response.setResponseMessage("Category Id is missing");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Category category = null;

		Optional<Category> optional = categoryDao.findById(categoryId);

		if (optional.isPresent()) {
			category = optional.get();
		}

		if (category == null) {
			response.setResponseMessage("Category not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.BAD_REQUEST);

		}

		List<Product> products = new ArrayList<Product>();

		products = productDao.findByCategoryId(categoryId);

		if (CollectionUtils.isEmpty(products)) {
			response.setResponseMessage("Products not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.OK);
		}

		response.setProducts(products);
		response.setResponseMessage("Product Fetched Successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<ProductResponse>(response, HttpStatus.OK);
	}

	public void fetchProductImage(String productImageName, HttpServletResponse resp) {
		Resource resource = storageService.load(productImageName);
		if (resource != null) {
			try (InputStream in = resource.getInputStream()) {
				ServletOutputStream out = resp.getOutputStream();
				FileCopyUtils.copy(in, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
