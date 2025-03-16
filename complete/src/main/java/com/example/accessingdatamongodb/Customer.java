package com.example.accessingdatamongodb;

import org.springframework.data.annotation.Id;


public class Customer {

	@Id
	public String id;

	public String firstName;
	public String lastName;
	public String socialSecurityNumber;

	public Customer() {}

	public Customer(String firstName, String lastName, String socialSecurityNumber) {
		this.firstName = firstName;
		this.lastName = lastName;
        this.socialSecurityNumber = socialSecurityNumber;
    }

	@Override
	public String toString() {
		return String.format(
				"Customer[id=%s, firstName='%s', lastName='%s', ssn='%s']",
				id, firstName, lastName, socialSecurityNumber);
	}

}

