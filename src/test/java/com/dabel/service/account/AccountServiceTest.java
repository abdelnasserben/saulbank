package com.dabel.service.account;

import com.dabel.DBSetupForTests;
import com.dabel.constant.*;
import com.dabel.dto.*;
import com.dabel.exception.ResourceNotFoundException;
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
class AccountServiceTest {
    @Autowired
    AccountService accountService;

    @Autowired
    BranchService branchService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    AccountDto accountDto;
    BranchDto savedBranch;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
        savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status(Status.ACTIVE.code())
                .build());
        accountDto = AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .currency(Currency.KMF.name())
                .accountType(AccountType.CURRENT.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .status(Status.ACTIVE.code())
                .branch(savedBranch)
                .build();
    }

    @Test
    void shouldSaveNewAccount() {
        //given
        //when
        AccountDto expected = accountService.save(accountDto);

        //then
        assertThat(expected.getAccountId()).isGreaterThan(0);
        assertThat(expected.getCreatedAt()).isNotNull();
        assertThat(expected.getAccountName()).isEqualTo("John Doe");
        assertThat(expected.getBranch().getBranchName()).isEqualTo("HQ");
    }

    @Test
    void shouldFindAccountByNumber() {
        //given
        accountService.save(accountDto);

        //when
        AccountDto expected = accountService.findByNumber("123456789");

        //then
        assertThat(expected.getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldFindAllAccounts() {
        //given
        accountService.save(accountDto);

        //when
        List<AccountDto> expected = accountService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsAccount() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountService.findByNumber("0123456"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindVaultByBranchAndCurrency() {
        //given
        accountDto.setIsVault(1);
        AccountDto savedAccount = accountService.save(accountDto);

        //when
        AccountDto expected = accountService.findVault(savedAccount.getBranch(), Currency.KMF.name());

        //then
        assertThat(expected.getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsVault() {
        //given
        AccountDto savedAccount = accountService.save(accountDto);

        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountService.findVault(savedAccount.getBranch(), Currency.USD.name()));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldSaveTrunk() {
        //given
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(accountDto.getBranch())
                .build());
        TrunkDto trunkDto = TrunkDto.builder()
                .customer(savedCustomer)
                .account(accountDto)
                .membership(AccountMembership.OWNER.name())
                .build();

        //when
        TrunkDto expected = accountService.save(trunkDto);

        //then
        assertThat(expected.getTrunkId()).isGreaterThan(0);
        assertThat(expected.getCustomer().getIdentityNumber()).isEqualTo("NBE123456");
    }

    @Test
    void shouldFindTrunkByAccountNumber() {
        //given
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(accountDto.getBranch())
                .build());
        accountService.save(TrunkDto.builder()
                .customer(savedCustomer)
                .account(accountDto)
                .membership(AccountMembership.OWNER.name())
                .build());

        //when
        TrunkDto expected = accountService.findTrunkByNumber("123456789");

        //then
        assertThat(expected.getAccount().getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsTrunk() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountService.findTrunkByNumber("0123456"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldSaveNewLedger() {
        //given
        LedgerDto ledgerDto = LedgerDto.builder()
                .branch(savedBranch)
                .account(accountDto)
                .ledgerType(LedgerType.WITHDRAW.name())
                .build();

        //when
        LedgerDto expected = accountService.save(ledgerDto);

        //then
        assertThat(expected.getLedgerId()).isGreaterThan(0);
        assertThat(expected.getAccount().getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldFindLedgerByAccountAndLedgerType() {
        //given
        accountService.save(LedgerDto.builder()
                .branch(savedBranch)
                .account(accountDto)
                .ledgerType(LedgerType.WITHDRAW.name())
                .build());

        //when
        LedgerDto expected = accountService.findLedgerByBranchAndType(savedBranch, LedgerType.WITHDRAW.name());

        //then
        assertThat(expected.getAccount().getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsLedger() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountService.findLedgerByBranchAndType(savedBranch, LedgerType.WITHDRAW.name()));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }
}