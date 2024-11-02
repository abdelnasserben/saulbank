package com.dabel.service.transaction;

import com.dabel.DBSetupForTests;
import com.dabel.constant.TransactionType;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DepositTest {

    @Autowired
    Deposit deposit;

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

        //TODO: create vault because a deposit requires one.
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
                .transactionType("DEPOSIT")
                .initiatorAccount(savedAccount)
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
    void shouldInitiateDeposit() {
        //given
        //when
        deposit.init(getTransactionDto());
        TransactionDto expected = transactionService.findAll().get(0);

        //then
        assertThat(expected.getTransactionId()).isGreaterThan(0);
    }

    @Test
    void shouldThrowExceptionWhenInitiatingDepositOnAnInactiveAccount() {
        //given
        TransactionDto transactionDto = TransactionDto.builder()
                .initiatorAccount(AccountDto.builder()
                        .status("5") //deactivated status = 5
                        .build())
                .build();

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> deposit.init(transactionDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account must be active");
    }

    @Test
    void shouldApproveDeposit() {
        //given
        deposit.init(getTransactionDto());
        TransactionDto initiatedTransaction = transactionService.findAll().get(0);

        //when
        deposit.approve(initiatedTransaction);

        //then
        assertThat(initiatedTransaction.getStatus()).isEqualTo("3"); //approved status = 3
    }

    @Test
    void shouldReturnDepositAsTransactionType() {
        assertThat(deposit.getType()).isEqualTo(TransactionType.DEPOSIT);
    }
}