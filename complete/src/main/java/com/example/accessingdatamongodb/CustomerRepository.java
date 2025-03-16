package com.example.accessingdatamongodb;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {

	Customer findByFirstName(String firstName);
	List<Customer> findByLastName(String lastName);

	Customer findBySocialSecurityNumber(String socialSecurityNumber);

	Customer findByLastNameAndSocialSecurityNumber(String lastName, String socialSecurityNumber);
}
