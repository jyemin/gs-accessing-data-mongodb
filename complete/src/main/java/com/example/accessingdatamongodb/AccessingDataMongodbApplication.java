package com.example.accessingdatamongodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccessingDataMongodbApplication implements CommandLineRunner {

	@Autowired
	private CustomerRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(AccessingDataMongodbApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		repository.deleteAll();

		// save a couple of customers
		repository.save(new Customer("Alice", "Smith", "123-45-6789"));
		repository.save(new Customer("Bob", "Smith", "123-45-0000"));

		// fetch all customers
		System.out.println("Customers found with findAll():");
		System.out.println("-------------------------------");
		for (Customer customer : repository.findAll()) {
			System.out.println(customer);
		}
		System.out.println();

		// fetch an individual customer
		System.out.println("Customer found with findByFirstName('Alice'):");
		System.out.println("--------------------------------");
		System.out.println(repository.findByFirstName("Alice"));
		System.out.println();

		System.out.println("Customers found with findByLastName('Smith'):");
		System.out.println("--------------------------------");
		for (Customer customer : repository.findByLastName("Smith")) {
			System.out.println(customer);
		}
		System.out.println();

		System.out.println("Customer found with findBySocialSecurityNumber('123-45-6789') (Encrypted SSN):");
		System.out.println("--------------------------------");
		Customer c = repository.findBySocialSecurityNumber("123-45-6789");
		System.out.println(c);
		System.out.println();

		System.out.println("Customer saved with new social security number('987-65-4321') (Encrypted SSN):");
		System.out.println("--------------------------------");
		c.socialSecurityNumber = "987-65-4321";
		repository.save(c);
		System.out.println();

		System.out.println("Customer found with findBySocialSecurityNumber('987-65-4321') (Encrypted SSN):");
		System.out.println("--------------------------------");
		c = repository.findBySocialSecurityNumber("987-65-4321");
		System.out.println(c);
		System.out.println();

		System.out.println("Customer found with findByLastNameAndSocialSecurityNumber('Smith', '987-65-4321') (Encrypted SSN):");
		System.out.println("--------------------------------");
		c = repository.findByLastNameAndSocialSecurityNumber("Smith", "987-65-4321");
		System.out.println(c);
	}

}
