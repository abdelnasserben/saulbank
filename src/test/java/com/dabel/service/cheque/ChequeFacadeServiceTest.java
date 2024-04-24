package com.dabel.service.cheque;

import com.dabel.DBSetupForTests;
import com.dabel.dto.*;
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
class ChequeFacadeServiceTest {

    @Autowired
    ChequeFacadeService chequeFacadeService;

    @Autowired
    BranchService branchService;

    @Autowired
    AccountService accountService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private void saveTrunk() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());

        AccountDto savedAccount = accountService.save(AccountDto.builder()
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

        accountService.save(TrunkDto.builder()
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
    void shouldSendRequest() {
        //given
        saveTrunk();
        PostChequeRequestDto postChequeRequestDto = PostChequeRequestDto.builder()
                .accountNumber("123456789")
                .customerIdentityNumber("NBE451287")
                .build();

        //when
        chequeFacadeService.sendRequest(postChequeRequestDto);
        ChequeRequestDto expected = chequeFacadeService.findAllRequests().get(0);

        //then
        assertThat(expected.getRequestId()).isGreaterThan(0);
        assertThat(expected.getStatus()).isEqualTo("0"); //pending status = 0
    }

    @Test
    void shouldActivateActiveCheque() {
        //given
        ChequeDto savedCheque = chequeFacadeService.saveCheque(ChequeDto.builder()
                .chequeNumber("00012367")
                .status("0")
                .build());

        //when
       chequeFacadeService.activateCheque(savedCheque.getChequeId());
       ChequeDto expected = chequeFacadeService.findAllCheques().get(0);

        //then
        assertThat(expected.getStatus()).isEqualTo("1");
    }

    @Test
    void shouldThrowExceptionWhenTryingActivateActiveCheque() {
        //given
        ChequeDto savedCheque = chequeFacadeService.saveCheque(ChequeDto.builder()
                .chequeNumber("00012367")
                .status("1")
                .build());

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> chequeFacadeService.activateCheque(savedCheque.getChequeId()));

        //then
        assertThat(expected.getMessage()).isEqualTo("Cheque already active");
    }

    @Test
    void shouldDeactivateCheque() {
        //given
        ChequeDto savedCheque = chequeFacadeService.saveCheque(ChequeDto.builder()
                .chequeNumber("00012367")
                .status("1")
                .build());

        //when
        chequeFacadeService.deactivateCheque(savedCheque.getChequeId(), "Just a reason");
        ChequeDto expected = chequeFacadeService.findAllCheques().get(0);

        //then
        assertThat(expected.getStatus()).isEqualTo("5"); //deactivated status = 5
        assertThat(expected.getFailureReason()).isEqualTo("Just a reason");
    }

    @Test
    void shouldThrowExceptionWhenTryingDeactivateInactiveCheque() {
        //given
        ChequeDto savedCheque = chequeFacadeService.saveCheque(ChequeDto.builder()
                .chequeNumber("00012367")
                .status("0")
                .build());

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> chequeFacadeService.deactivateCheque(savedCheque.getChequeId(), "Just reason"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Unable to deactivate an inactive cheque");
    }

    @Test
    void shouldInitiateChequePayment() {
        //given
        saveTrunk();

        //TODO: save receiver account because a cheque payment requires one.
        AccountDto receiverAccount = accountService.save(AccountDto.builder()
                .accountName("Sarah Hunt")
                .accountNumber("0987654321")
                .currency("KMF")
                .accountType("SAVING")
                .accountProfile("PERSONAL")
                .status("1")
//                .branch(savedBranch)
                .build());

        //TODO: save ledger for cheque application fees
        accountService.save(LedgerDto.builder()
                .ledgerType("CHEQUE_REQUEST")
                .account(AccountDto.builder()
                        .accountName("Cheque ledger")
                        .accountNumber("0213456587")
                        .accountType("BUSINESS")
                        .accountProfile("PERSONAL")
                        .currency("KMF")
//                        .branch(requestDto.getBranch())
                        .status("1")
                        .build())
                .build());
    }
}