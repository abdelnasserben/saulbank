package com.dabel.service.card;

import com.dabel.DBSetupForTests;
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
class CardRequestOperationServiceTest {

    @Autowired
    CardRequestOperationService cardRequestOperationService;

    @Autowired
    BranchService branchService;

    @Autowired
    AccountService accountService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private CardRequestDto getCardRequestDto() {
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
                .identityNumber("NBE123456")
                .status("1")
                .branch(savedBranch)
                .build());

        TrunkDto savedTrunk = accountService.save(TrunkDto.builder()
                .account(savedAccount)
                .customer(savedCustomer)
                .membership("OWNER")
                .build());

        return CardRequestDto.builder()
                .trunk(savedTrunk)
                .cardType("VISA")
                .status("0")
                .branch(savedBranch)
                .build();
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldGetCardRequestService() {
        assertThat(cardRequestOperationService.getCardRequestService()).isNotNull();
    }

    @Test
    void shouldInitiateCardRequest() {
        //given
        //when
        cardRequestOperationService.init(getCardRequestDto());
        CardRequestDto expected = cardRequestOperationService.getCardRequestService().findAll().get(0);

        //then
        assertThat(expected.getRequestId()).isGreaterThan(0);
    }

    @Test
    void shouldThrowExceptionWhenRequestCardForInactiveAccount() {
        //given
        CardRequestDto requestDto = getCardRequestDto();
        requestDto.getTrunk().getAccount().setStatus("5"); //deactivated status = 5

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> cardRequestOperationService.init(requestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("The account is not eligible for this operation");
    }

    @Test
    void shouldThrowExceptionWhenRequestCardForAssociativeAccount() {
        //given
        CardRequestDto requestDto = getCardRequestDto();
        requestDto.getTrunk().getAccount().setAccountProfile("ASSOCIATIVE");

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> cardRequestOperationService.init(requestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("The account is not eligible for this operation");
    }

    @Test
    void shouldThrowExceptionWhenRequestCardForAccountWhoseOwnerIsInactive() {
        //given
        CardRequestDto requestDto = getCardRequestDto();
        requestDto.getTrunk().getAccount().setStatus("5"); //deactivated status = 5

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> cardRequestOperationService.init(requestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("The account is not eligible for this operation");
    }

    @Test
    void shouldThrowExceptionWhenRequestCardWithInsufficientBalance() {
        //given
        CardRequestDto requestDto = getCardRequestDto();
        requestDto.getTrunk().getAccount().setBalance(50);

        //when
        Exception expected = assertThrows(BalanceInsufficientException.class, () -> cardRequestOperationService.init(requestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account balance is insufficient for application fees");
    }

    @Test
    void shouldApproveCardRequest() {
        //given
        CardRequestDto requestDto = getCardRequestDto();

        //TODO: save ledger for card application fees
        accountService.save(LedgerDto.builder()
                .ledgerType("CARD_REQUEST")
                .account(AccountDto.builder()
                        .accountName("Card ledger")
                        .accountNumber("0213456587")
                        .accountType("BUSINESS")
                        .accountProfile("PERSONAL")
                        .currency("KMF")
                        .branch(requestDto.getBranch())
                        .status("1")
                        .build())
                .build());

        cardRequestOperationService.init(requestDto);
        CardRequestDto initiatedRequest = cardRequestOperationService.getCardRequestService().findAll().get(0);

        //when
        cardRequestOperationService.approve(initiatedRequest);

        //then
        assertThat(initiatedRequest.getStatus()).isEqualTo("3"); //approved status = 3
    }

    @Test
    void shouldRejectCardRequest() {
        //given
        cardRequestOperationService.init(getCardRequestDto());
        CardRequestDto initiatedRequest = cardRequestOperationService.getCardRequestService().findAll().get(0);

        //when
        cardRequestOperationService.reject(initiatedRequest, "Just a reject reason");

        //then
        assertThat(initiatedRequest.getStatus()).isEqualTo("4"); //Rejected status = 4
        assertThat(initiatedRequest.getFailureReason()).isEqualTo("Just a reject reason");
    }
}