package com.dabel.service.transaction;

import com.dabel.DBSetupForTests;
import com.dabel.constant.TransactionType;
import com.dabel.dto.*;
import com.dabel.exception.BalanceInsufficientException;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import com.dabel.service.cheque.ChequeRequestService;
import com.dabel.service.cheque.ChequeService;
import com.dabel.service.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ChequePaymentTest {

    @Autowired
    ChequePayment chequePayment;

    @Autowired
    ChequeService chequeService;

    @Autowired
    ChequeRequestService chequeRequestService;

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



    private TrunkDto getTrunkDto() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());

        AccountDto initiator = accountService.saveAccount(AccountDto.builder()
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


        return TrunkDto.builder()
                .customer(savedCustomer)
                .account(initiator)
                .membership("OWNER")
                .build();
    }

    private TransactionDto getTransactionDto() {

        //TODO: save trunk because a cheque payment requires one.
        TrunkDto savedTrunk = accountService.saveTrunk(getTrunkDto());

        //TODO: create receiver account because a transfer requires one.
        AccountDto receiverAccount = accountService.saveAccount(AccountDto.builder()
                .accountName("Sarah Hunt")
                .accountNumber("0057654321")
                .currency("KMF")
                .accountType("SAVING")
                .accountProfile("PERSONAL")
                .status("1")
                .branch(savedTrunk.getAccount().getBranch())
                .build());

        return TransactionDto.builder()
                .transactionType("WITHDRAW")
                .initiatorAccount(savedTrunk.getAccount())
                .customerIdentity("NBE451287")
                .receiverAccount(receiverAccount)
                .currency("KMF")
                .amount(500)
                .sourceType("ONLINE")
                .reason("Just a test")
                .branch(savedTrunk.getAccount().getBranch())
                .build();
    }

    //TODO: make sure a trunk is saved before calling this method
    private void saveCheque() {
        TrunkDto savedTrunk = accountService.findAllTrunks().get(0);
        ChequeRequestDto requestDto = chequeRequestService.save(ChequeRequestDto.builder()
                .trunk(savedTrunk)
                .status("0")
                .branch(savedTrunk.getAccount().getBranch())
                .build());

        chequeService.save(ChequeDto.builder()
                .chequeNumber("12345678")
                .serial(requestDto)
                .trunk(savedTrunk)
                .branch(savedTrunk.getAccount().getBranch())
                .status("0")
                .build());
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldInitiateChequePayment() {
        //given
        //when
        chequePayment.init(getTransactionDto());
        TransactionDto expected = transactionService.findAll().get(0);

        //then
        assertThat(expected.getTransactionId()).isGreaterThan(0);
    }

    @Test
    void shouldThrowExceptionWhenInitiatingChequePaymentOnAnInactiveAccount() {
        //given
        TransactionDto transactionDto = TransactionDto.builder()
                .initiatorAccount(AccountDto.builder()
                        .status("5") //deactivated status = 5
                        .build())
                .build();

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> chequePayment.init(transactionDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Initiator account must be active");
    }

    @Test
    void shouldThrowExceptionWhenInitiatingChequePaymentInACurrencyOtherThanThatOfTheInitiatorAccount() {
        //given
        TransactionDto transactionDto = getTransactionDto();
        transactionDto.setCurrency("USD");

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> chequePayment.init(transactionDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("The transaction currency must match that of the account (KMF)");
    }

    @Test
    void shouldThrowExceptionWhenInitiatingSelfChequePayment() {
        //given
        TransactionDto transactionDto = getTransactionDto();
        transactionDto.setReceiverAccount(transactionDto.getInitiatorAccount());

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> chequePayment.init(transactionDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Can't make self transfer");
    }

    @Test
    void shouldThrowExceptionWhenInitiatingChequePaymentWithInsufficientBalance() {
        //given
        TransactionDto transactionDto = getTransactionDto();
        transactionDto.setAmount(80000);

        //when
        Exception expected = assertThrows(BalanceInsufficientException.class, () -> chequePayment.init(transactionDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account balance is insufficient");
    }

    @Test
    void shouldApproveChequePayment() {
        //given
        TransactionDto transactionDto = getTransactionDto();

        //TODO: save cheque because cheque payment approval update cheque status
        saveCheque();

        //TODO: modify the transaction, the source value is the cheque number
        transactionDto.setSourceValue("12345678");

        chequePayment.init(transactionDto);
        TransactionDto initiatedTransaction = transactionService.findAll().get(0);

        //when
        chequePayment.approve(initiatedTransaction);
        ChequeDto expectedChequePayed = chequeService.findAll().get(0);

        //then
        assertThat(initiatedTransaction.getStatus()).isEqualTo("3"); //approved status = 3
        assertThat(expectedChequePayed.getStatus()).isEqualTo("6"); //used status = 6
    }

    @Test
    void shouldReturnChequePaymentAsTransactionType() {
        assertThat(chequePayment.getType()).isEqualTo(TransactionType.CHEQUE_PAYMENT);
    }
}