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
class AccountFacadeServiceTest {

    @Autowired
    AccountFacadeService accountFacadeService;

    @Autowired
    BranchService branchService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    AccountDto accountDto;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status(Status.ACTIVE.code())
                .build());

        accountDto = AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .currency(Currency.KMF.name())
                .accountType(AccountType.SAVING.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .status(Status.ACTIVE.code())
                .branch(savedBranch)
                .build();
    }

    @Test
    void shouldSaveAccount() {
        //given
        //when
        AccountDto expected = accountFacadeService.save(accountDto);

        //then
        assertThat(expected.getAccountId()).isGreaterThan(0);
        assertThat(expected.getAccountNumber()).isEqualTo("123456789");
        assertThat(expected.getBranch().getBranchName()).isEqualTo("HQ");
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
        TrunkDto expected = accountFacadeService.save(trunkDto);

        //then
        assertThat(expected.getTrunkId()).isGreaterThan(0);
        assertThat(expected.getCustomer().getIdentityNumber()).isEqualTo("NBE123456");
    }

    @Test
    void shouldSaveLedger() {
        //given
        LedgerDto ledgerDto = LedgerDto.builder()
                .branch(accountDto.getBranch())
                .account(accountDto)
                .ledgerType(LedgerType.WITHDRAW.name())
                .build();

        //when
        LedgerDto expected = accountFacadeService.save(ledgerDto);

        //then
        assertThat(expected.getLedgerId()).isGreaterThan(0);
        assertThat(expected.getAccount().getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldFindAllAccounts() {
        //given
        accountFacadeService.save(accountDto);

        //when
        List<AccountDto> expected = accountFacadeService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldFindAccountByHisNumber() {
        //given
        accountFacadeService.save(accountDto);

        //when
        AccountDto expected = accountFacadeService.findByNumber("123456789");

        //then
        assertThat(expected.getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsAccount() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountFacadeService.findByNumber("0123456"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindAllVault() {
        //given
        accountDto.setIsVault(1);
        accountFacadeService.save(accountDto);

        //when
//        List<AccountDto> expected = accountFacadeService.findAllVault(accountDto.getBranch());

        //then
//        assertThat(expected.size()).isEqualTo(1);
//        assertThat(expected.get(0).getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldFindVaultByHisBranchAndCurrency() {
        //given
        accountDto.setIsVault(1);
        accountFacadeService.save(accountDto);

        //when
//        AccountDto expected = accountFacadeService.findVault(accountDto.getBranch(), "KMF");

        //then
//        assertThat(expected.getAccountNumber()).isEqualTo("123456789");
//        assertThat(expected.getCurrency()).isEqualTo("KMF");
//        assertThat(expected.getBranch().getBranchName()).isEqualTo("HQ");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsVault() {
        //given
        //when
//        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountFacadeService.findVault(accountDto.getBranch(), "USD"));

        //then
//        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindAllBranchLedgers() {
        //given
        accountFacadeService.save(LedgerDto.builder()
                .branch(accountDto.getBranch())
                .account(accountDto)
                .ledgerType(LedgerType.WITHDRAW.name())
                .build());

        //when
//        List<LedgerDto> expected = accountFacadeService.findAllLedgers(accountDto.getBranch());

        //then
//        assertThat(expected.size()).isEqualTo(1);
//        assertThat(expected.get(0).getAccount().getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void findLedgerByBranchAndType() {
        //given
        accountFacadeService.save(LedgerDto.builder()
                .branch(accountDto.getBranch())
                .account(accountDto)
                .ledgerType(LedgerType.WITHDRAW.name())
                .build());

        //when
//        LedgerDto expected = accountFacadeService.findLedgerByBranchAndType(accountDto.getBranch(), "WITHDRAW");

        //then
//        assertThat(expected.getAccount().getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsLedger() {
        //given
        //when
//        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountFacadeService.findLedgerByBranchAndType(accountDto.getBranch(), "WITHDRAW"));

        //then
//        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindAllTrunks() {
        //given
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(accountDto.getBranch())
                .build());
        accountFacadeService.save(TrunkDto.builder()
                .customer(savedCustomer)
                .account(accountDto)
                .membership(AccountMembership.OWNER.name())
                .build());

        //when
        List<TrunkDto> expected = accountFacadeService.findAllTrunks();

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getAccount().getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldFindTrunkByHisId() {
        //given
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(accountDto.getBranch())
                .build());
        TrunkDto savedTrunk = accountFacadeService.save(TrunkDto.builder()
                .customer(savedCustomer)
                .account(accountDto)
                .membership(AccountMembership.OWNER.name())
                .build());

        //when
        TrunkDto expected = accountFacadeService.findTrunkById(savedTrunk.getTrunkId());

        //then
        assertThat(expected.getAccount().getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsTrunkByHisId() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountFacadeService.findTrunkById(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindTrunkByHisNumber() {
        //given
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(accountDto.getBranch())
                .build());
        accountFacadeService.save(TrunkDto.builder()
                .customer(savedCustomer)
                .account(accountDto)
                .membership(AccountMembership.OWNER.name())
                .build());

        //when
        TrunkDto expected = accountFacadeService.findTrunkByNumber("123456789");

        //then
        assertThat(expected.getAccount().getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsTrunkByHisNumber() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountFacadeService.findTrunkByNumber("00000"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void shouldFindTrunkByCustomerAndAccountNumber() {
        //given
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(accountDto.getBranch())
                .build());
        accountFacadeService.save(TrunkDto.builder()
                .customer(savedCustomer)
                .account(accountDto)
                .membership(AccountMembership.OWNER.name())
                .build());

        //when
        TrunkDto expected = accountFacadeService.findTrunkByCustomerAndAccountNumber(savedCustomer,"123456789");

        //then
        assertThat(expected.getAccount().getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsTrunkByCustomerAndAccountNumber() {
        //given
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(accountDto.getBranch())
                .build());
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> accountFacadeService.findTrunkByCustomerAndAccountNumber(savedCustomer, "0000"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account not found");
    }

    @Test
    void findAllTrunksByCustomer() {
        //given
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(accountDto.getBranch())
                .build());
        accountFacadeService.save(TrunkDto.builder()
                .customer(savedCustomer)
                .account(accountDto)
                .membership(AccountMembership.OWNER.name())
                .build());

        //when
        List<TrunkDto> expected = accountFacadeService.findAllTrunks(savedCustomer);

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getAccount().getAccountName()).isEqualTo("John Doe");
    }

    @Test
    void shouldActivateTrunkByHisId() {
        //given
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(accountDto.getBranch())
                .build());

        accountDto.setStatus(Status.PENDING.code());
        TrunkDto trunkDto = TrunkDto.builder()
                .customer(savedCustomer)
                .account(accountDto)
                .membership(AccountMembership.OWNER.name())
                .build();
        TrunkDto savedTrunk = accountFacadeService.save(trunkDto);

        //when
        accountFacadeService.activateTrunk(savedTrunk.getTrunkId());
        TrunkDto expected = accountFacadeService.findTrunkById(savedTrunk.getTrunkId());

        //then
        assertThat(savedTrunk.getAccount().getStatus()).isEqualTo(Status.PENDING.code());
        assertThat(expected.getAccount().getStatus()).isEqualTo(Status.ACTIVE.code());
    }

    @Test
    void deactivateTrunk() {
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
        TrunkDto savedTrunk = accountFacadeService.save(trunkDto);

        //when
        accountFacadeService.deactivateTrunk(savedTrunk.getTrunkId());
        TrunkDto expected = accountFacadeService.findTrunkById(savedTrunk.getTrunkId());

        //then
        assertThat(savedTrunk.getAccount().getStatus()).isEqualTo(Status.ACTIVE.code());
        assertThat(expected.getAccount().getStatus()).isEqualTo(Status.DEACTIVATED.code());
    }
}