package com.dabel.service.account;

import com.dabel.DBSetupForTests;
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


    private AccountDto getAccountDto() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());

        return AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .currency("KMF")
                .balance(0)
                .accountType("SAVING")
                .accountProfile("PERSONAL")
                .status("1")
                .branch(savedBranch)
                .build();
    }

    private AccountDto saveAccount() {
        return accountService.save(getAccountDto());
    }

    private AccountDto saveVault() {
        AccountDto accountDto = getAccountDto();
        accountDto.setIsVault(1);
        return accountService.save(accountDto);
    }

    private TrunkDto saveTrunk() {
        AccountDto savedAccount = saveAccount();
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status("1")
                .branch(savedAccount.getBranch())
                .build());
        TrunkDto trunkDto = TrunkDto.builder()
                .customer(savedCustomer)
                .account(savedAccount)
                .membership("OWNER")
                .build();

        return accountService.save(trunkDto);
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldSaveAccount() {
        //given
        //when
        AccountDto expected = accountService.save(getAccountDto());

        //then
        assertThat(expected.getAccountId()).isGreaterThan(0);
        assertThat(expected.getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldSaveTrunk() {
        //given
        AccountDto savedAccount = saveAccount();
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status("1")
                .branch(savedAccount.getBranch())
                .build());
        TrunkDto trunkDto = TrunkDto.builder()
                .customer(savedCustomer)
                .account(savedAccount)
                .membership("OWNER")
                .build();

        //when
        TrunkDto expected = accountService.save(trunkDto);

        //then
        assertThat(expected.getTrunkId()).isGreaterThan(0);
        assertThat(expected.getCustomer().getIdentityNumber()).isEqualTo("NBE123456");
    }

    @Test
    void shouldSaveLedger() {
        //given
        AccountDto savedAccount = saveAccount();
        LedgerDto ledgerDto = LedgerDto.builder()
                .branch(savedAccount.getBranch())
                .account(savedAccount)
                .ledgerType("WITHDRAW")
                .build();

        //when
        LedgerDto expected = accountService.save(ledgerDto);

        //then
        assertThat(expected.getLedgerId()).isGreaterThan(0);
        assertThat(expected.getAccount().getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldFindAllAccounts() {
        //given
        saveAccount();

        //when
        List<AccountDto> expected = accountService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldFindAccountByHisNumber() {
        //given
        saveAccount();

        //when
        AccountDto expected = accountService.findByNumber("123456789");

        //then
        assertThat(expected.getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindAccountByNonExistentNumber() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountService.findByNumber("0123456"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindAllBranchVaultsByBranch() {
        //given
        AccountDto savedVault = saveVault();

        //when
        List<AccountDto> expected = accountService.findAllVaults(savedVault.getBranch());

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldFindVaultByBranchAndCurrency() {
        //given
        AccountDto savedVault = saveVault();

        //when
        AccountDto expected = accountService.findVault(savedVault.getBranch(), "KMF");

        //then
        assertThat(expected.getAccountNumber()).isEqualTo("123456789");
        assertThat(expected.getCurrency()).isEqualTo("KMF");
        assertThat(expected.getBranch().getBranchName()).isEqualTo("HQ");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindNonExistentVault() {
        //given
        AccountDto savedAccount = saveAccount();
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountService.findVault(savedAccount.getBranch(), "USD"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindAllBranchLedgersByBranch() {
        //given
        AccountDto savedAccount = saveAccount();
        accountService.save(LedgerDto.builder()
                .account(savedAccount)
                .ledgerType("WITHDRAW")
                .branch(savedAccount.getBranch())
                .build());

        //when
        List<LedgerDto> expected = accountService.findAllLedgers(savedAccount.getBranch());

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getAccount().getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldFindLedgerByBranchAndType() {
        //given
        AccountDto savedAccount = saveAccount();
        accountService.save(LedgerDto.builder()
                .account(savedAccount)
                .ledgerType("WITHDRAW")
                .branch(savedAccount.getBranch())
                .build());

        //when
        LedgerDto expected = accountService.findLedgerByBranchAndType(savedAccount.getBranch(), "WITHDRAW");

        //then
        assertThat(expected.getAccount().getAccountNumber()).isEqualTo("123456789");
    }@Test
    void shouldThrowExceptionWhenTryingToFindNonExistentLedger() {
        //given
        AccountDto savedAccount = saveAccount();
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountService.findLedgerByBranchAndType(savedAccount.getBranch(), "WITHDRAW"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindAllTrunks() {
        //given
        saveTrunk();

        //when
        List<TrunkDto> expected = accountService.findAllTrunks();

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getAccount().getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldFindTrunksByCustomer() {
        //given
        CustomerDto savedCustomer = saveTrunk().getCustomer();

        //when
        List<TrunkDto> expected = accountService.findAllTrunks(savedCustomer);

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getAccount().getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldFindTrunksByAccount() {
        //given
        AccountDto savedAccount = saveTrunk().getAccount();

        //when
        List<TrunkDto> expected = accountService.findAllTrunks(savedAccount);

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getAccount().getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldFindTrunkByHisId() {
        //given
        TrunkDto savedTrunk = saveTrunk();

        //when
        TrunkDto expected = accountService.findTrunk(savedTrunk.getTrunkId());

        //then
        assertThat(expected.getAccount().getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindTrunkByNonExistentId() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountService.findTrunk(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindTrunkByHisNumber() {
        //given
        saveTrunk();

        //when
        TrunkDto expected = accountService.findTrunk("123456789");

        //then
        assertThat(expected.getAccount().getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindTrunkByNonExistentNumber() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountService.findTrunk("00000"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindTrunkByCustomerAndAccountNumber() {
        //given
        CustomerDto savedCustomer = saveTrunk().getCustomer();

        //when
        TrunkDto expected = accountService.findTrunk(savedCustomer,"123456789");

        //then
        assertThat(expected.getAccount().getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindTrunkByNonExistentCustomerAndAccountNumber() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountService.findTrunk(CustomerDto.builder().build(), "0000"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldDeleteTrunk() {
        //given
        TrunkDto savedTrunk = saveTrunk();

        //when
        List<TrunkDto> expectedNotEmptyTrunksList = accountService.findAllTrunks();
        accountService.deleteTrunk(savedTrunk);
        List<TrunkDto> expectedEmptyTrunksList = accountService.findAllTrunks();

        //then
        assertThat(expectedNotEmptyTrunksList.size()).isEqualTo(1);
        assertThat(expectedEmptyTrunksList.size()).isEqualTo(0);
    }


    @Test
    void shouldDebitAccount() {
        //given
        AccountDto savedAccount = saveAccount();

        //when
        accountService.debit(savedAccount, 500);
        AccountDto expected = accountService.findAll().get(0);

        //then
        assertThat(expected.getBalance()).isEqualTo(-500.0);
    }

    @Test
    void shouldCreditAccount() {
        //given
        AccountDto savedAccount = saveAccount();

        //when
        accountService.credit(savedAccount, 500);
        AccountDto expected = accountService.findAll().get(0);

        //then
        assertThat(expected.getBalance()).isEqualTo(500.0);
    }
}