package com.dabel.service.customer;

import com.dabel.app.Helper;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.service.account.AccountService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;

@Service
public class CustomerFacadeService {

    private final CustomerService customerService;
    private final AccountService accountService;

    public CustomerFacadeService(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    public CustomerDto saveCustomer(CustomerDto customerDto) {
        return customerService.save(customerDto);
    }

    public void createNewCustomerWithAccount(CustomerDto customerDto, String accountName, AccountType accountType, AccountProfile accountProfile) {

        if (customerDto.getCustomerId() != null)
            return;

        String currentUsername = Helper.getAuthenticated().getName();

        //TODO: save customer
        customerDto.setStatus(Status.ACTIVE.code());
        customerDto.setInitiatedBy(currentUsername);
        CustomerDto savedCustomer = customerService.save(customerDto);

        //TODO: define the membership and save trunk
        String accountMembership = accountProfile.equals(AccountProfile.ASSOCIATIVE) ? AccountMembership.ASSOCIATED.name() : AccountMembership.OWNER.name();
        accountService.saveTrunk(TrunkDto.builder()
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

    public List<CustomerDto> getAll() {
        return customerService.findAll();
    }

    public CustomerDto getByIdentityNumber(String identityNumber) {
        return customerService.findByIdentity(identityNumber);
    }

    public CustomerDto getById(Long customerId) {
        return customerService.findById(customerId);
    }

    public void updateCustomerDetails(CustomerDto customerDto) {

        CustomerDto existingCustomer = getById(customerDto.getCustomerId());

        updateNonEmptyFields(existingCustomer, customerDto);

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

    private void updateNonEmptyFields(CustomerDto target, CustomerDto source) {
        for (Field field : CustomerDto.class.getDeclaredFields()) {
            field.setAccessible(true);

            try {
                Object newValue = field.get(source);
                Object existingValue = field.get(target);

                if (newValue != null && !newValue.toString().isEmpty() && !newValue.equals(existingValue)) {
                    field.set(target, newValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field during update", e);
            }
        }
    }
}
