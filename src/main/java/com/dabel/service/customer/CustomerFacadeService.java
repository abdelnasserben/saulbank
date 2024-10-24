package com.dabel.service.customer;

import com.dabel.app.Helper;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
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

        String currentUsername = Helper.getAuthenticated().getName();

        //TODO: save customer
        customerDto.setStatus(Status.ACTIVE.code());
        customerDto.setInitiatedBy(currentUsername);
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
                        .initiatedBy(currentUsername)
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

        CustomerDto existingCustomer = findById(customerDto.getCustomerId());

        for(Field field: CustomerDto.class.getDeclaredFields()) {
            field.setAccessible(true); //allow access private field

            try {
                Object newValue = field.get(customerDto);
                Object existingValue = field.get(existingCustomer);

                // Check if the new value is not null, not empty and different of the old value
                if (newValue != null && !newValue.toString().isEmpty() && !newValue.equals(existingValue)) {
                    field.set(existingCustomer, newValue);
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        existingCustomer.setStatus(Status.codeOf(customerDto.getStatus()));
        existingCustomer.setUpdatedBy(Helper.getAuthenticated().getName());
        customerService.save(existingCustomer);
    }

    public double getCompletionRate(CustomerDto customerDto) {
        int totalFields = 0;
        int filledFields = 0;

        for (Field field : CustomerDto.class.getDeclaredFields()) {
            field.setAccessible(true);
            totalFields++;

            try {
                Object value = field.get(customerDto);
                if (value != null && !value.toString().isEmpty())
                    filledFields++;

            } catch (IllegalAccessException ignored) {}
        }

        return (double) filledFields / totalFields * 100;
    }
}
