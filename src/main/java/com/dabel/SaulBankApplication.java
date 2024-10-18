package com.dabel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaulBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaulBankApplication.class, args);
	}

}
//loan operation service for fees [loan must join by the beneficiary account]
//manage user [add fields email, firstName and lastName, details, update password, etc...]
//role management [only user role can make some operations]
