package com.dabel.service.customer;

import com.dabel.app.Helper;
import com.dabel.constant.AccountMembership;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.Currency;
import com.dabel.constant.Status;
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

    public void create(CustomerDto customerDTO, String accountType) {

        if (customerDTO.getCustomerId() != null)
            return;

        customerDTO.setStatus(Status.ACTIVE.code());
        CustomerDto savedCustomer = customerService.save(customerDTO);

        //TODO: create trunk kmf
        accountService.save(TrunkDto.builder()
                .customer(savedCustomer)
                .account(AccountDto.builder()
                        .accountName(String.format("%s %s", savedCustomer.getFirstName(), savedCustomer.getLastName()))
                        .accountNumber(Helper.generateAccountNumber())
                        .accountType(accountType)
                        .accountProfile(AccountProfile.PERSONAL.name())
                        .currency(Currency.KMF.name())
                        .branch(savedCustomer.getBranch())
                        .status(Status.ACTIVE.code())
                        .build())
                .membership(AccountMembership.OWNER.name())
                .build());
    }

    public List<CustomerDto> findAll() {
        return customerService.findAll();
    }

    public CustomerDto findByIdentity(String identityNumber) {
        return customerService.findByIdentity(identityNumber);
    }

    public CustomerDto findById(Long customerId) {
        return customerService.findById(customerId);
    }
}
