package com.dabel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaulBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaulBankApplication.class, args);
	}

}
//withdraw future(only owner customer can init / display signature picture on init transaction)
//begin cheques futures implementation