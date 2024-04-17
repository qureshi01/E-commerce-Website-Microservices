package com.ecommerce.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ecommerce.user.model.Address;

@Repository
public interface AddressDao extends JpaRepository<Address, Integer> {

}
