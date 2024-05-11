package com.dabel;

import com.dabel.dto.BranchDto;
import com.dabel.dto.UserDto;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SaulBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaulBankApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(BranchFacadeService branchFacadeService, UserService userService) {
		return args -> {

			//TODO: create branch
			branchFacadeService.create(BranchDto.builder()
					.branchName("HQ")
					.branchAddress("Moroni, Place de la France")
					.build(), new double[]{5000, 3000, 1000});

			//TODO: create user
			UserDto userDto = UserDto.builder()
					.username("john")
					.role("ADMIN")
					.branch(branchFacadeService.findAll().get(0))
					.build();

			userService.create(userDto);
		};
	}

}
//users roles [Cashier, Manager, Loaner, Receptionist, Admin, BO(back office)]
//transaction details, display initiator and receiver account
