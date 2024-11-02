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

/**
 * Service for affiliating and disaffiliating customers to/from accounts.
 */
@Service
public class AccountAffiliationService {

    @Getter
    private final AccountService accountService;
    private final CustomerService customerService;

    public AccountAffiliationService(AccountService accountService, CustomerService customerService) {
        this.accountService = accountService;
        this.customerService = customerService;
    }

    /**
     * Affiliates a customer with a specified account. If the customer does not exist,
     * a new customer is created. The account profile is updated if necessary.
     *
     * @param customerDto  The data transfer object representing the customer.
     * @param accountNumber The account number with which to affiliate the customer.
     * @throws IllegalOperationException if the account is not active or the customer is already affiliated.
     */
    public void affiliate(CustomerDto customerDto, String accountNumber) {

        AccountDto accountDto = fetchActiveAccount(accountNumber);

        if (customerDto.getCustomerId() != null) {
            validateCustomer(customerDto);
            checkCustomerAffiliation(customerDto, accountNumber);
        } else {
            customerDto = saveNewCustomer(customerDto);
        }

        saveNewTrunk(customerDto, accountDto);
        updateAccountProfileToJointIfPersonal(accountDto);

    }

    /**
     * Disaffiliates a customer from a specified account. If the customer is the last member,
     * the account profile is updated accordingly.
     *
     * @param customerDto  The data transfer object representing the customer.
     * @param accountNumber The account number from which to disaffiliate the customer.
     * @throws ResourceNotFoundException if the trunk associated with the customer and account is not found.
     */
    public void disaffiliate(CustomerDto customerDto, String accountNumber) {
        TrunkDto trunkDto = accountService.findTrunkByCustomerAndAccountNumber(customerDto, accountNumber);
        if (AppSpEL.removableMember(trunkDto)) {
            accountService.deleteTrunk(trunkDto);
        }

        updateAccountProfileToPersonalIfSingleMember(trunkDto);
    }

    /**
     * Saves a new customer in the system with active status and records the initiator.
     *
     * @param customerDto The data transfer object representing the customer to be saved.
     * @return The saved CustomerDto.
     */
    private CustomerDto saveNewCustomer(CustomerDto customerDto) {
        customerDto.setStatus(Status.ACTIVE.code());
        customerDto.setInitiatedBy(Helper.getAuthenticated().getName());
        return customerService.save(customerDto);
    }

    /**
     * Fetches an active account based on the provided account number.
     *
     * @param accountNumber The account number of the account to fetch.
     * @return The AccountDto corresponding to the account number.
     * @throws IllegalOperationException if the account is not active.
     */
    private AccountDto fetchActiveAccount(String accountNumber) {
        AccountDto accountDto = accountService.findTrunkByAccountNumber(accountNumber).getAccount();
        if (!Helper.isActiveStatedObject(accountDto)) {
            throw new IllegalOperationException("Account must be active");
        }
        return accountDto;
    }

    /**
     * Validates the provided customer to ensure they are active.
     *
     * @param customerDto The customer to validate.
     * @throws IllegalOperationException if the customer is inactive.
     */
    private void validateCustomer(CustomerDto customerDto) {
        if (!Helper.isActiveStatedObject(customerDto)) {
            throw new IllegalOperationException(customerDto.getFirstName() + " is inactive");
        }
    }

    /**
     * Checks if a customer is already affiliated with a specified account.
     *
     * @param customerDto  The customer to check for affiliation.
     * @param accountNumber The account number to check against.
     * @throws IllegalOperationException if the customer is already affiliated.
     */
    private void checkCustomerAffiliation(CustomerDto customerDto, String accountNumber) {
        try {
            TrunkDto trunkDto = accountService.findTrunkByCustomerAndAccountNumber(customerDto, accountNumber);
            if (trunkDto != null) {
                throw new IllegalOperationException(customerDto.getFirstName() + " is already a member");
            }
        } catch (ResourceNotFoundException ignored) {
            // Not affiliated, continue
        }
    }

    /**
     * Saves a new trunk linking a customer with an account, setting the appropriate membership type.
     *
     * @param customerDto The customer to be affiliated.
     * @param accountDto  The account to which the customer is being affiliated.
     */
    private void saveNewTrunk(CustomerDto customerDto, AccountDto accountDto) {
        TrunkDto newTrunk = TrunkDto.builder()
                .customer(customerDto)
                .account(accountDto)
                .membership(Helper.isAssociativeAccount(accountDto) ? AccountMembership.ASSOCIATED.name() : AccountMembership.JOINTED.name())
                .build();
        accountService.saveTrunk(newTrunk);
    }

    /**
     * Updates account profile to "JOINT" if it is currently "PERSONAL".
     *
     * @param accountDto Account to update.
     */
    private void updateAccountProfileToJointIfPersonal(AccountDto accountDto) {
        if (Helper.isPersonalAccount(accountDto)) {
            accountDto.setAccountProfile(AccountProfile.JOINT.name());
            accountDto.setUpdatedBy(Helper.getAuthenticated().getName());
            accountService.saveAccount(accountDto);
        }
    }

    /**
     * Updates the account profile to personal if the customer was the last member of a joint account.
     *
     * @param trunkDto The trunk linking the customer and account.
     */
    private void updateAccountProfileToPersonalIfSingleMember(TrunkDto trunkDto) {
        AccountDto accountDto = trunkDto.getAccount();
        if (!Helper.isAssociativeAccount(accountDto)) {
            List<TrunkDto> trunks = accountService.findAllTrunksByAccount(accountDto);
            if (trunks.size() == 1) {
                accountDto.setAccountProfile(AccountProfile.PERSONAL.name());
                accountDto.setUpdatedBy(Helper.getAuthenticated().getName());
                accountService.saveAccount(accountDto);
            }
        }
    }
}
