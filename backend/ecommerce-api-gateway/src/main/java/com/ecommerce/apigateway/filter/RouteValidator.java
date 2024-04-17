package com.ecommerce.apigateway.filter;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
    		"/api/user/register",
  		     "/api/user/login",
  		     "/api/user/forget-password",
  		     "/api/user/fetch",
  		     "/api/product/all",
  		     "/api/product/category/all",
  		     "/api/product/category/fetch",
  		     "/api/product/id"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> { 
            	System.out.println(request.getURI().getPath());         	
            	if(request.getURI().getPath().contains("/api/product/image") ) {
            		return false;
            	}
            	return openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
            	
            };

}
