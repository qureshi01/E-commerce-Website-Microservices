package com.ecommerce.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.user.dao.UserDao;
import com.ecommerce.user.model.User;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserDao userDao;

	@Override
	public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {

		User user = this.userDao.findByEmailId(emailId);

		return org.springframework.security.core.userdetails.User.withUsername(user.getEmailId())
				.password(user.getPassword()).authorities(user.getRole()).build();

	}

}
