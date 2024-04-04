package com.dabel.service.account;

import com.dabel.app.Helper;
import com.dabel.config.AppSpEL;
import com.dabel.constant.AccountMembership;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.customer.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;

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
            accountService.findTrunkByCustomerAndAccountNumber(customerDto, accountNumber);
            throw new IllegalOperationException("Account already exists");
        }catch (Exception ignored) {}

        //TODO: get the trunk
        TrunkDto trunkDto = accountService.findTrunkByNumber(accountNumber);
        AccountDto accountDto = trunkDto.getAccount();

        //TODO: save customer if doesn't exists
        if(customerDto.getCustomerId() == null) {
            customerDto.setStatus(Status.ACTIVE.code());
            customerDto = customerService.save(customerDto);
        }

        //TODO: save new associated if it's an associative account
        if(accountDto.getAccountProfile().equals(AccountProfile.ASSOCIATIVE.name())) {
            accountService.save(TrunkDto.builder()
                    .account(accountDto)
                    .customer(customerDto)
                    .membership(AccountMembership.ASSOCIATED.name())
                    .build());
            return;
        }

        //TODO: update account profile to JOINT if is PERSONAL
        if(accountDto.getAccountProfile().equals(AccountProfile.PERSONAL.name())) {
            accountDto.setAccountProfile(AccountProfile.JOINT.name());
            accountService.save(accountDto);
        }

        //TODO: save new jointed
        accountService.save(TrunkDto.builder()
                .account(accountDto)
                .customer(customerDto)
                .membership(AccountMembership.JOINTED.name())
                .build());
    }

    public void remove(CustomerDto customerDto, String accountNumber) {
        TrunkDto trunkDto = accountService.findTrunkByCustomerAndAccountNumber(customerDto, accountNumber);
        if(AppSpEL.removableMember(trunkDto))
            accountService.deleteTrunk(trunkDto);

        AccountDto accountDto = trunkDto.getAccount();

        if(!Helper.isAssociativeAccount(accountDto)) {
            List<TrunkDto> trunks = accountService.findAllTrunks(accountDto);
            if(trunks.size() == 1) {
                accountDto.setAccountProfile(AccountProfile.PERSONAL.name());
                accountService.save(accountDto);
            }
        }
    }
}
