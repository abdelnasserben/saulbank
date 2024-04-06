package com.dabel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaulBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaulBankApplication.class, args);
	}

}
//continue upload customer files (on update info, documents upload). see the spring boot tutorial
//correction of account affiliation (only active account and active customer can make affiliation)
//begin cheques futures implementation