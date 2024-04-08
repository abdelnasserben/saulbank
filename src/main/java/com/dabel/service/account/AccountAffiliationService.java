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
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.service.customer.CustomerService;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountAffiliationService {

    @Getter
    private final AccountService accountService;
    private final CustomerService customerService;

    public AccountAffiliationService(AccountService accountService, CustomerService customerService) {
        this.accountService = accountService;
        this.customerService = customerService;
    }

    public void add(CustomerDto customerDto, String accountNumber) {

        //TODO: get the account
        AccountDto accountDto = accountService.findTrunkByNumber(accountNumber).getAccount();

        if(Helper.isInactiveAccount(accountDto))
            throw new IllegalOperationException("Account must be active");

        //TODO: check if is exists customer
        if(customerDto.getCustomerId() != null) {

            if (Helper.isInactiveCustomer(customerDto))
                throw new IllegalOperationException(customerDto.getFirstName() + " is inactive");

            //TODO: check if customer is already affiliated
            TrunkDto trunkDto = null;
            try {
                trunkDto = accountService.findTrunkByCustomerAndAccountNumber(customerDto, accountNumber);
            }catch (ResourceNotFoundException ignored) {}

            if(trunkDto != null)
                throw new IllegalOperationException(customerDto.getFirstName() + " is already member");

        } else {
            //TODO: save new customer
            customerDto.setStatus(Status.ACTIVE.code());
            customerDto = customerService.save(customerDto);
        }

        //TODO: save the new trunk
        TrunkDto newTrunk = TrunkDto.builder()
                .customer(customerDto)
                .account(accountDto)
                .membership(Helper.isAssociativeAccount(accountDto) ? AccountMembership.ASSOCIATED.name() : AccountMembership.JOINTED.name())
                .build();
        accountService.save(newTrunk);

        //TODO: update account profile to JOINT if is PERSONAL
        if(Helper.isPersonalAccount(accountDto)) {
            accountDto.setAccountProfile(AccountProfile.JOINT.name());
            accountService.save(accountDto);
        }

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
