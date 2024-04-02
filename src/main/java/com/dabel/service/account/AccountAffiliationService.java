package com.dabel.service.account;

import com.dabel.config.AppSpEL;
import com.dabel.constant.AccountMembership;
import com.dabel.constant.AccountProfile;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.customer.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class AccountAffiliationService {

    protected final AccountService accountService;
    protected final CustomerService customerService;

    protected AccountAffiliationService(AccountService accountService, CustomerService customerService) {
        this.accountService = accountService;
        this.customerService = customerService;
    }

    public void add(CustomerDto customerDto, String accountNumber) {

        //TODO: check whether the customer is already affiliated to this account
        try {
            TrunkDto trunkDto = accountService.findTrunkByCustomerAndAccountNumber(customerDto, accountNumber);
            throw new IllegalOperationException("Account already exists");
        }catch (Exception ignored) {}

        //TODO: get the trunk
        TrunkDto trunkDto = accountService.findTrunkByNumber(accountNumber);
        AccountDto accountDto = trunkDto.getAccount();
        String accountProfile = trunkDto.getAccount().getAccountProfile();

        if(accountProfile.equals(AccountProfile.ASSOCIATIVE.name())) {
            accountService.save(TrunkDto.builder()
                    .account(accountDto)
                    .customer(customerDto)
                    .membership(AccountMembership.JOINTED.name())
                    .build());
            return;
        }


        if(accountProfile.equals(AccountProfile.PERSONAL.name())) {
            accountDto.setAccountProfile(AccountProfile.JOINT.name());
            accountService.save(accountDto);
        }

        accountService.save(TrunkDto.builder()
                .account(accountDto)
                .customer(customerDto)
                .membership(AccountMembership.JOINTED.name())
                .build());
    }

    public void remove(String customerIdentityNumber, String accountNumber) {
        CustomerDto customerDto = customerService.findByIdentity(customerIdentityNumber);
        TrunkDto trunkDto = accountService.findTrunkByCustomerAndAccountNumber(customerDto, accountNumber);
        if(AppSpEL.removableMember(trunkDto))
            accountService.deleteTrunk(trunkDto);
    }
}
