package com.dabel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaulBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaulBankApplication.class, args);
	}

}
//users roles [Cashier, Manager, Loaner, Receptionist, Admin, BO(back office)]
//transaction details, display initiator and receiver account
