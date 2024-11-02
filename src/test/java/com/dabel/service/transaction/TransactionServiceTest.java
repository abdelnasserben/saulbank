package com.dabel.service.transaction;

import com.dabel.DBSetupForTests;
import com.dabel.constant.Currency;
import com.dabel.constant.TransactionType;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TransactionServiceTest {
    @Autowired
    TransactionService transactionService;

    @Autowired
    BranchService branchService;

    @Autowired
    AccountService accountService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private TransactionDto getTransactionDto() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());

        AccountDto savedAccount1 = accountService.saveAccount(AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .status("1")
                .branch(savedBranch)
                .build());

        AccountDto savedAccount2 = accountService.saveAccount(AccountDto.builder()
                .accountName("Sarah Hunt")
                .accountNumber("987654321")
                .status("1")
                .branch(savedBranch)
                .build());

        return TransactionDto.builder()
                .transactionType(TransactionType.DEPOSIT.name())
                .initiatorAccount(savedAccount1)
                .receiverAccount(savedAccount2)
                .currency(Currency.KMF.name())
                .amount(500)
                .branch(savedBranch)
                .customerIdentity("NBE453890")
                .customerFullName("John Doe")
                .reason("Just for test")
                .build();
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldSaveTransaction() {
        //given
        //when
        TransactionDto expected = transactionService.save(getTransactionDto());

        //then
        assertThat(expected.getTransactionId()).isGreaterThan(0);
    }

    @Test
    void shouldFindAllTransactions() {
        //given
        transactionService.save(getTransactionDto());

        //when
        List<TransactionDto> expected = transactionService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldFindTransactionById() {
        //given
        TransactionDto savedTransaction = transactionService.save(getTransactionDto());

        //when
        TransactionDto expected = transactionService.findById(savedTransaction.getTransactionId());

        //then
        assertThat(expected.getTransactionId()).isGreaterThan(0);
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindTransactionByNonExistentId() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> transactionService.findById(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Transaction not found");
    }

    @Test
    void shouldFindTransactionsByAccount() {
        //given
        TransactionDto savedTransaction = transactionService.save(getTransactionDto());

        //when
        List<TransactionDto> expected = transactionService.findAllByAccount(savedTransaction.getInitiatorAccount());

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getInitiatorAccount().getAccountName()).isEqualTo("John Doe");
    }
}