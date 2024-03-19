package com.dabel.service.transaction;

import com.dabel.DBSetupForTests;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TransactionDto;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import com.dabel.service.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransactionServiceTest {
    @Autowired
    TransactionService transactionService;

    @Autowired
    AccountService accountService;

    @Autowired
    BranchService branchService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status(Status.ACTIVE.code())
                .build());
        AccountDto savedAccount1 = accountService.save(AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .currency(Currency.KMF.name())
                .accountType(AccountType.SAVING.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .status(Status.ACTIVE.code())
                .branch(savedBranch)
                .build());

        AccountDto savedAccount2 = accountService.save(AccountDto.builder()
                .accountName("Sarah Hunt")
                .accountNumber("987654321")
                .currency(Currency.KMF.name())
                .accountType(AccountType.SAVING.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .status(Status.ACTIVE.code())
                .branch(savedBranch)
                .build());

        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(savedBranch)
                .build());

        transactionDto = TransactionDto.builder()
                .transactionType(TransactionType.DEPOSIT.name())
                .initiatorAccount(savedAccount1)
                .receiverAccount(savedAccount2)
                .currency(Currency.KMF.name())
                .amount(500)
                .branch(savedBranch)
                .customer(savedCustomer)
                .reason("Just for test")
                .build();
    }

    @Test
    void shouldSaveNewTransaction() {
        //given
        //when
        TransactionDto expected = transactionService.save(transactionDto);

        //then
        assertThat(expected.getTransactionId()).isGreaterThan(0);
        assertThat(expected.getInitiatorAccount().getAccountName()).isEqualTo("John Doe");
        assertThat(expected.getReceiverAccount().getAccountName()).isEqualTo("Sarah Hunt");
        assertThat(expected.getCustomer().getIdentityNumber()).isEqualTo("NBE123456");
    }

    @Test
    void shouldFindAllTransactions() {
        //given
        transactionService.save(transactionDto);

        //when
        List<TransactionDto> expected = transactionService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldFindTransactionById() {
        //given
        TransactionDto savedTransaction = transactionService.save(transactionDto);

        //when
        TransactionDto expected = transactionService.findById(savedTransaction.getTransactionId());

        //then
        assertThat(expected.getTransactionId()).isGreaterThan(0);
    }
}