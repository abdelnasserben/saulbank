package com.dabel.service.transaction;

import com.dabel.DBSetupForTests;
import com.dabel.constant.TransactionType;
import com.dabel.dto.*;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import com.dabel.service.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WithdrawTest {

    @Autowired
    Withdraw withdraw;

    @Autowired
    TransactionService transactionService;

    @Autowired
    BranchService branchService;

    @Autowired
    AccountService accountService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private TransactionDto getTransactionDto() {

        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());

        AccountDto savedAccount = accountService.saveAccount(AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .currency("KMF")
                .balance(25000)
                .accountType("SAVING")
                .accountProfile("PERSONAL")
                .status("1")
                .branch(savedBranch)
                .build());

        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE451287")
                .status("1")
                .branch(savedBranch)
                .build());

        //TODO: create trunk because a withdraw requires one.
        accountService.saveTrunk(TrunkDto.builder()
                .customer(savedCustomer)
                .account(savedAccount)
                .membership("OWNER")
                .build());

        //TODO: create vault because a withdraw requires one.
        accountService.saveAccount(AccountDto.builder()
                .accountName("Vault Code 1")
                .accountNumber("987654321")
                .currency("KMF")
                .accountType("BUSINESS")
                .accountProfile("PERSONAL")
                .isVault(1)
                .status("1")
                .branch(savedBranch)
                .build());

        return TransactionDto.builder()
                .transactionType("WITHDRAW")
                .initiatorAccount(savedAccount)
                .customerIdentity("NBE451287")
                .currency("KMF")
                .amount(500)
                .sourceType("ONLINE")
                .reason("Just a test")
                .branch(savedBranch)
                .build();
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldInitiateWithdraw() {
        //given
        //when
        withdraw.init(getTransactionDto());
        TransactionDto expected = transactionService.findAll().get(0);

        //then
        assertThat(expected.getTransactionId()).isGreaterThan(0);
    }

    @Test
    void shouldThrowExceptionWhenInitiatingWithdrawalOnAnInactiveAccount() {
        //given
        TransactionDto transactionDto = TransactionDto.builder()
                .initiatorAccount(AccountDto.builder()
                        .status("5") //deactivated status = 5
                        .build())
                .build();

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> withdraw.init(transactionDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account must be active");
    }

    @Test
    void shouldThrowExceptionWhenInitiatingAWithdrawalInACurrencyOtherThanThatOfTheAccount() {
        //given
        TransactionDto transactionDto = getTransactionDto();
        transactionDto.setCurrency("USD");

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> withdraw.init(transactionDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("The transaction currency must match that of the account (KMF)");
    }

    @Test
    void shouldThrowExceptionWhenInitiatingWithdrawalWithInsufficientBalance() {
        //given
        TransactionDto transactionDto = getTransactionDto();
        transactionDto.setAmount(80000);

        //when
        Exception expected = assertThrows(BalanceInsufficientException.class, () -> withdraw.init(transactionDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account balance is insufficient");
    }

    @Test
    void shouldApproveWithdraw() {
        //given
        TransactionDto transactionDto = getTransactionDto();

        //TODO: create with ledger for withdraw fees
        accountService.saveLedger(LedgerDto.builder()
                .ledgerType("WITHDRAW")
                .account(AccountDto.builder()
                        .accountName("Withdraw ledger")
                        .accountNumber("0213456587")
                        .accountType("BUSINESS")
                        .accountProfile("PERSONAL")
                        .currency("KMF")
                        .branch(transactionDto.getBranch())
                        .status("1")
                        .build())
                .build());

        withdraw.init(transactionDto);
        TransactionDto initiatedTransaction = transactionService.findAll().get(0);

        //when
        withdraw.approve(initiatedTransaction);

        //then
        assertThat(initiatedTransaction.getStatus()).isEqualTo("3"); //approved status = 3
    }

    @Test
    void shouldReturnWithdrawAsTransactionType() {
        assertThat(withdraw.getType()).isEqualTo(TransactionType.WITHDRAW);
    }
}