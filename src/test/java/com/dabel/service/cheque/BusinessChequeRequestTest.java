package com.dabel.service.cheque;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountType;
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
class BusinessChequeRequestTest {

    @Autowired
    BusinessChequeRequest businessChequeRequest;

    @Autowired
    BranchService branchService;

    @Autowired
    AccountService accountService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private ChequeRequestDto getChequeRequestDto() {
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
                .accountType("BUSINESS")
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

        TrunkDto savedTrunk = accountService.saveTrunk(TrunkDto.builder()
                .customer(savedCustomer)
                .account(savedAccount)
                .membership("OWNER")
                .build());

        return ChequeRequestDto.builder()
                .trunk(savedTrunk)
                .status("0")
                .branch(savedBranch)
                .build();
    }


    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldRequestChequesForBusinessAccount() {
        //given
        //when
        businessChequeRequest.init(getChequeRequestDto());
        ChequeRequestDto expected = businessChequeRequest.chequeRequestService.findAll().get(0);

        //then
        assertThat(expected.getRequestId()).isGreaterThan(0);
    }

    @Test
    void shouldThrowExceptionWhenRequestChequesForNonBusinessAccount() {
        //given
        ChequeRequestDto requestDto = getChequeRequestDto();
        requestDto.getTrunk().getAccount().setAccountType("SAVING");

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> businessChequeRequest.init(requestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Only business account is eligible for this operation");
    }

    @Test
    void shouldThrowExceptionWhenRequestChequesForInactiveBusinessAccount() {
        //given
        ChequeRequestDto requestDto = getChequeRequestDto();
        requestDto.getTrunk().getAccount().setStatus("5"); //deactivated status = 5

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> businessChequeRequest.init(requestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("The account and its owner must be active for this operation");
    }

    @Test
    void shouldThrowExceptionWhenRequestChequesForBusinessAccountWhoseOwnerIsInactive() {
        //given
        ChequeRequestDto requestDto = getChequeRequestDto();
        requestDto.getTrunk().getCustomer().setStatus("5"); //deactivated status = 5

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> businessChequeRequest.init(requestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("The account and its owner must be active for this operation");
    }

    @Test
    void shouldThrowExceptionWhenRequestChequesForBusinessAccountWithInsufficientBalance() {
        //given
        ChequeRequestDto requestDto = getChequeRequestDto();
        requestDto.getTrunk().getAccount().setBalance(50);

        //when
        Exception expected = assertThrows(BalanceInsufficientException.class, () -> businessChequeRequest.init(requestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account balance is insufficient for application fees");
    }

    @Test
    void shouldApproveRequestChequesForBusinessAccount() {
        //given
        ChequeRequestDto requestDto = getChequeRequestDto();

        //TODO: save ledger for cheque application fees
        accountService.saveLedger(LedgerDto.builder()
                .ledgerType("CHEQUE_REQUEST")
                .account(AccountDto.builder()
                        .accountName("Cheque ledger")
                        .accountNumber("0213456587")
                        .accountType("BUSINESS")
                        .accountProfile("PERSONAL")
                        .currency("KMF")
                        .branch(requestDto.getBranch())
                        .status("1")
                        .build())
                .build());

        businessChequeRequest.init(requestDto);
        ChequeRequestDto initiatedRequest = businessChequeRequest.chequeRequestService.findAll().get(0);

        //when
        businessChequeRequest.approve(initiatedRequest);

        //then
        assertThat(initiatedRequest.getStatus()).isEqualTo("3"); //approved status = 3
    }

    @Test
    void shouldRejectRequestChequesForBusinessAccount() {

        businessChequeRequest.init(getChequeRequestDto());
        ChequeRequestDto initiatedRequest = businessChequeRequest.chequeRequestService.findAll().get(0);

        //when
        businessChequeRequest.reject(initiatedRequest, "Just a reject reason");

        //then
        assertThat(initiatedRequest.getStatus()).isEqualTo("4"); //Rejected status = 4
        assertThat(initiatedRequest.getFailureReason()).isEqualTo("Just a reject reason");
    }

    @Test
    void shouldReturnSBusinessAsChequeRequestType() {
        assertThat(businessChequeRequest.getAccountType()).isEqualTo(AccountType.BUSINESS);
    }
}