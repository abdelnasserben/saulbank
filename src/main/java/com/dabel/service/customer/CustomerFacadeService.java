package com.dabel.service.customer;

import com.dabel.app.Generator;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Currency;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.service.account.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerFacadeService {

    private final CustomerService customerService;
    private final AccountService accountService;

    public CustomerFacadeService(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    public void create(CustomerDto customerDTO) {

        if (customerDTO.getCustomerId() == null) {
            CustomerDto savedCustomer = customerService.save(customerDTO);

            //TODO: create trunk kmf
            accountService.save(TrunkDto.builder()
                    .customer(savedCustomer)
                    .account(AccountDto.builder()
                            .accountName(String.format("%s %s", savedCustomer.getFirstName(), savedCustomer.getLastName()))
                            .accountNumber(Generator.generateAccountNumber())
                            .accountType(AccountType.CURRENT.name())
                            .accountProfile(AccountProfile.PERSONAL.name())
                            .currency(Currency.KMF.name())
                            .branch(savedCustomer.getBranch())
                            .status(savedCustomer.getStatus())
                            .build())
                    .build());

        } else customerService.save(customerDTO);
    }

    public List<CustomerDto> findAll() {
        return customerService.findAll();
    }

    public CustomerDto findByIdentity(String identityNumber) {
        return customerService.findByIdentity(identityNumber);
    }
}
