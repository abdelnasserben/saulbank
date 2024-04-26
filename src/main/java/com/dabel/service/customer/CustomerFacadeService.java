package com.dabel.service.customer;

import com.dabel.app.Helper;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerFacadeService {

    private final CustomerService customerService;
    private final AccountService accountService;

    @Autowired
    public CustomerFacadeService(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    public CustomerDto save(CustomerDto customerDto) {
        return customerService.save(customerDto);
    }

    public void create(CustomerDto customerDto, String accountName, AccountType accountType, AccountProfile accountProfile) {

        if (customerDto.getCustomerId() != null)
            return;

        //TODO: save customer
        customerDto.setStatus(Status.ACTIVE.code());
        CustomerDto savedCustomer = customerService.save(customerDto);

        //TODO: define the membership and save trunk
        String accountMembership = accountProfile.equals(AccountProfile.ASSOCIATIVE) ? AccountMembership.ASSOCIATED.name() : AccountMembership.OWNER.name();
        accountService.save(TrunkDto.builder()
                .customer(savedCustomer)
                .account(AccountDto.builder()
                        .accountName(accountName)
                        .accountNumber(Helper.generateAccountNumber())
                        .accountType(accountType.name())
                        .accountProfile(accountProfile.name())
                        .currency(Currency.KMF.name())
                        .branch(savedCustomer.getBranch())
                        .status(Status.ACTIVE.code())
                        .build())
                .membership(accountMembership)
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

    public void update(CustomerDto customerDto) {
        customerDto.setStatus(Status.codeOf(customerDto.getStatus()));
        customerService.save(customerDto);
    }
}
