package com.ecommerce.product.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.product.model.Category;

public interface CategoryDao extends JpaRepository<Category, Integer> {
	

}
