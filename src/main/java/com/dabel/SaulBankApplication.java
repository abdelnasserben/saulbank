package com.dabel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaulBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaulBankApplication.class, args);
	}

}
//begin cheques futures implementation ....
//card request, make card depend on trunk instead customer and account repeat
//card request and cheque request. only account and customer actives can send request
//remove test to check all unnecessary methods on service classes
