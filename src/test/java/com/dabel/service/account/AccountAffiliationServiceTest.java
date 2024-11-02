package com.dabel.service.account;

import com.dabel.DBSetupForTests;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.branch.BranchService;
import com.dabel.service.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AccountAffiliationServiceTest {

    @Autowired
    AccountAffiliationService accountAffiliationService;

    @Autowired
    BranchService branchService;

    @Autowired
    AccountService accountService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private BranchDto savedBranch() {
        return branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());
    }

    private TrunkDto saveTrunk(String accountNumber, String accountStatus, String customerFirstName, String customerLastName, String customerIdentity, String customerStatus) {

        BranchDto savedBranch = savedBranch();

        AccountDto savedAccount = accountService.saveAccount(AccountDto.builder()
                .accountNumber(accountNumber)
                .accountName(String.format("%s %s", customerFirstName, customerLastName))
                .currency("KMF")
                .balance(0)
                .accountType("SAVING")
                .accountProfile("PERSONAL")
                .status(accountStatus)
                .branch(savedBranch)
                .build());

        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName(customerFirstName)
                .lastName(customerLastName)
                .identityNumber(customerIdentity)
                .status(customerStatus)
                .branch(savedBranch)
                .build());

        return accountService.saveTrunk(TrunkDto.builder()
                .customer(savedCustomer)
                .account(savedAccount)
                .membership("OWNER")
                .build());
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldJoinNonExistentCustomerToAPersonalAccount() {
        //given
        TrunkDto savedTrunk = saveTrunk("123456789", "1", "John", "Doe", "NBE123456", "1");

        CustomerDto newCustomer = CustomerDto.builder()
                .firstName("Sarah")
                .lastName("Hunt")
                .identityNumber("NBE001245")
                .branch(savedTrunk.getAccount().getBranch())
                .build();

        //when
        accountAffiliationService.affiliate(newCustomer, "123456789");
        CustomerDto savedNewCustomer = customerService.findByIdentity("NBE001245");
        List<TrunkDto> expectedNewCustomerTrunks = accountService.findAllTrunksByCustomer(savedNewCustomer);

        //then
        assertThat(expectedNewCustomerTrunks.size()).isEqualTo(1);
        assertThat(expectedNewCustomerTrunks.get(0).getAccount().getAccountProfile()).isEqualTo("JOINT");
        assertThat(expectedNewCustomerTrunks.get(0).getMembership()).isEqualTo("JOINTED");
    }

    @Test
    void shouldJoinAnExistingCustomerToAPersonalAccount() {
        //given
        saveTrunk("123456789", "1", "John", "Doe", "NBE123456", "1");
        TrunkDto sarahTrunk = saveTrunk("987654321", "1", "sarah", "Hunt", "NBE001245", "1");

        //when
        accountAffiliationService.affiliate(sarahTrunk.getCustomer(), "123456789");
        List<TrunkDto> expectedSarahTrunks = accountService.findAllTrunksByCustomer(sarahTrunk.getCustomer());

        //then
        assertThat(expectedSarahTrunks.size()).isEqualTo(2);
        assertThat(expectedSarahTrunks.get(0).getAccount().getAccountProfile()).isEqualTo("PERSONAL");
        assertThat(expectedSarahTrunks.get(0).getMembership()).isEqualTo("OWNER");
        assertThat(expectedSarahTrunks.get(1).getAccount().getAccountProfile()).isEqualTo("JOINT");
        assertThat(expectedSarahTrunks.get(1).getMembership()).isEqualTo("JOINTED");
    }

    @Test
    void shouldThrowExceptionWhenAffiliatingACustomerToAnInactiveAccount() {
        //given
        saveTrunk("123456789", "0", "John", "Doe", "NBE123456", "1");

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> accountAffiliationService.affiliate(CustomerDto.builder().build(), "123456789"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account must be active");
    }

    @Test
    void shouldThrowExceptionWhenAffiliatingAnInactiveCustomerToAnActiveAccount() {
        //given
        saveTrunk("123456789", "1", "John", "Doe", "NBE123456", "1");
        TrunkDto sarahTrunk = saveTrunk("987654321", "1", "Sarah", "Hunt", "NBE001245", "0");

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> accountAffiliationService.affiliate(sarahTrunk.getCustomer(), "123456789"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Sarah is inactive");
    }

    @Test
    void shouldThrowExceptionWhenAffiliatingACustomerWhoIsAlreadyAffiliatedToTheAccount() {
        //given
        TrunkDto sarahTrunk = saveTrunk("987654321", "1", "Sarah", "Hunt", "NBE001245", "1");

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> accountAffiliationService.affiliate(sarahTrunk.getCustomer(), "987654321"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Sarah is already member");
    }

    @Test
    void shouldDisaffiliateAnAffiliateFromAccount() {
        //given
        saveTrunk("123456789", "1", "John", "Doe", "NBE123456", "1");
        TrunkDto sarahTrunk = saveTrunk("987654321", "1", "sarah", "Hunt", "NBE001245", "1");

        accountAffiliationService.affiliate(sarahTrunk.getCustomer(), "123456789");

        //when
        accountAffiliationService.disaffiliate(sarahTrunk.getCustomer(), "123456789");
        List<TrunkDto> expectedSarahTrunksAfterDisaffiliating = accountService.findAllTrunksByCustomer(sarahTrunk.getCustomer());

        //then

        assertThat(expectedSarahTrunksAfterDisaffiliating.size()).isEqualTo(1);
        assertThat(expectedSarahTrunksAfterDisaffiliating.get(0).getAccount().getAccountProfile()).isEqualTo("PERSONAL");
        assertThat(expectedSarahTrunksAfterDisaffiliating.get(0).getMembership()).isEqualTo("OWNER");
    }

    @Test
    void shouldUpdateAccountProfileToPersonalWhenDisaffiliatingTheLastAffiliateFromAJointAccount() {
        //given
        TrunkDto johnTrunkBeforeAffiliating = saveTrunk("123456789", "1", "John", "Doe", "NBE123456", "1");
        TrunkDto sarahTrunk = saveTrunk("987654321", "1", "sarah", "Hunt", "NBE001245", "1");

        accountAffiliationService.affiliate(sarahTrunk.getCustomer(), "123456789");
        TrunkDto johnTrunkAfterAffiliating = accountService.findTrunkByAccountNumber("123456789");

        //when
        accountAffiliationService.disaffiliate(sarahTrunk.getCustomer(), "123456789");
        TrunkDto johnTrunkAfterDisaffiliating = accountService.findTrunkByAccountNumber("123456789");

        //then
        assertThat(johnTrunkBeforeAffiliating.getAccount().getAccountProfile()).isEqualTo("PERSONAL");
        assertThat(johnTrunkBeforeAffiliating.getMembership()).isEqualTo("OWNER");

        assertThat(johnTrunkAfterAffiliating.getAccount().getAccountProfile()).isEqualTo("JOINT");
        assertThat(johnTrunkAfterAffiliating.getMembership()).isEqualTo("OWNER");

        assertThat(johnTrunkAfterDisaffiliating.getAccount().getAccountProfile()).isEqualTo("PERSONAL");
        assertThat(johnTrunkAfterDisaffiliating.getMembership()).isEqualTo("OWNER");

    }
}