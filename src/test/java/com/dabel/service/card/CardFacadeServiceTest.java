package com.dabel.service.card;

import com.dabel.DBSetupForTests;
import com.dabel.constant.*;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CardFacadeServiceTest {

    @Autowired
    CardFacadeService cardFacadeService;

    @Autowired
    BranchService branchService;

    @Autowired
    AccountService accountService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    BranchDto savedBranch;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    private CardDto getCardDto() {
        savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status(Status.ACTIVE.code())
                .build());
        AccountDto savedAccount = accountService.save(AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .accountType(AccountType.SAVING.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.EUR.name())
                .branch(savedBranch)
                .build());

        return CardDto.builder()
                .cardName("John Doe")
                .cardNumber("1450 1525 1542 1248")
                .account(savedAccount)
                .expirationDate(LocalDate.of(24, 2, 26))
                .cvc("123")
                .build();
    }

    private CardRequestDto getCardRequestDto() {

        savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status(Status.ACTIVE.code())
                .build());

        AccountDto accountDto = AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .currency(Currency.KMF.name())
                .balance(75000)
                .accountType(AccountType.SAVING.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .status(Status.ACTIVE.code())
                .branch(savedBranch)
                .build();

        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(savedBranch)
                .build());

        TrunkDto savedTrunk = accountService.save(TrunkDto.builder()
                .account(accountDto)
                .customer(savedCustomer)
                .membership(AccountMembership.OWNER.name())
                .build());

        return CardRequestDto.builder()
                .cardType(CardType.VISA.name())
                .trunk(savedTrunk)
                .branch(savedBranch)
                .build();
    }

    @Test
    void shouldSaveNewCard() {
        //given
        //when
        cardFacadeService.saveCard(getCardDto());
        CardDto expected = cardFacadeService.findAllCards().get(0);

        //then
        assertThat(expected.getCardName()).isEqualTo("John Doe");
    }

    @Test
    void shouldFindCardByHisNumber() {
        //given
        cardFacadeService.saveCard(getCardDto());

        //when
        CardDto expected = cardFacadeService.findCardByCardNumber("1450 1525 1542 1248");

        //then
        assertThat(expected.getCardName()).isEqualTo("John Doe");
    }

    @Test
    void shouldFindCardByHisId() {
        //given
        cardFacadeService.saveCard(getCardDto());
        CardDto savedCard = cardFacadeService.findAllCards().get(0);

        //when
        CardDto expected = cardFacadeService.findCardById(savedCard.getCardId());

        //then
        assertThat(expected.getCardName()).isEqualTo("John Doe");
    }

    @Test
    void findAllCardsOdAnAccount() {
        //given
        cardFacadeService.saveCard(getCardDto());

        //when
        List<CardDto> expected = cardFacadeService.findAllCardsOfAnAccount("123456789");

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getCardName()).isEqualTo("John Doe");
    }

    @Test
    void shouldSendCardRequest() {
        //given
        //when
        cardFacadeService.sendRequest(getCardRequestDto());
        CardRequestDto expected = cardFacadeService.findAllCardRequests().get(0);

        //then
        assertThat(expected.getRequestId()).isGreaterThan(0);
        assertThat(expected.getCardType()).isEqualTo(CardType.VISA.name());
        assertThat(expected.getStatus()).isEqualTo(Status.PENDING.code());
    }

    @Test
    void shouldApproveCardRequest() {
        //given
        CardRequestDto cardRequestDto = getCardRequestDto();

        //we save ledger first
        accountService.save(LedgerDto.builder()
                .ledgerType(LedgerType.CARD_REQUEST.name())
                .account(AccountDto.builder()
                        .accountName("Card request ledger")
                        .accountNumber("0213456587")
                        .accountType(AccountType.BUSINESS.name())
                        .accountProfile(AccountProfile.PERSONAL.name())
                        .currency(Currency.KMF.name())
                        .branch(cardRequestDto.getBranch())
                        .status(Status.ACTIVE.code())
                        .build())
                .build());

        cardFacadeService.sendRequest(cardRequestDto);
        CardRequestDto savedCardRequest = cardFacadeService.findAllCardRequests().get(0);

        //when
        cardFacadeService.approveRequest(savedCardRequest.getRequestId());
        CardRequestDto expected = cardFacadeService.findAllCardRequests().get(0);

        //then
        assertThat(expected.getCardType()).isEqualTo(CardType.VISA.name());
        assertThat(expected.getStatus()).isEqualTo(Status.APPROVED.code());
    }

    @Test
    void shouldRejectCardRequest() {
        //given
        cardFacadeService.sendRequest(getCardRequestDto());
        CardRequestDto savedCardRequest = cardFacadeService.findAllCardRequests().get(0);

        //when
        cardFacadeService.rejectRequest(savedCardRequest.getRequestId(), "Simple remarks");
        CardRequestDto expected = cardFacadeService.findAllCardRequests().get(0);

        //then
        assertThat(expected.getStatus()).isEqualTo(Status.REJECTED.code());
        assertThat(expected.getFailureReason()).isEqualTo("Simple remarks");
    }

    @Test
    void shouldThrowExceptionWhenTrySendCardRequestOfAnInactiveAccount() {
        //given
        CardRequestDto cardRequestDto = CardRequestDto.builder()
                .cardType(CardType.MASTERCARD.name())
                .trunk(TrunkDto.builder()
                        .account(AccountDto.builder()
                                .accountName("John Doe")
                                .accountNumber("123456789")
                                .status(Status.PENDING.code())
                                .build())
                        .customer(CustomerDto.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .identityNumber("NBE587215")
                                .build())
                        .build())
                .build();

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> cardFacadeService.sendRequest(cardRequestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("The account is not eligible for this operation");
    }

    @Test
    void shouldThrowExceptionWhenTrySendCardRequestOfAnAssociativeAccount() {
        //given
        CardRequestDto cardRequestDto = CardRequestDto.builder()
                .cardType(CardType.MASTERCARD.name())
                .trunk(TrunkDto.builder()
                        .account(AccountDto.builder()
                                .accountProfile(AccountProfile.ASSOCIATIVE.name())
                                .accountName("John Doe")
                                .accountNumber("123456789")
                                .status(Status.ACTIVE.code())
                                .build())
                        .customer(CustomerDto.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .identityNumber("NBE587215")
                                .build())
                        .build())
                .build();

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> cardFacadeService.sendRequest(cardRequestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("The account is not eligible for this operation");
    }

    @Test
    void shouldThrowExceptionWhenTrySendCardRequestOfAnAccountWithInsufficientBalanceForRequestFees() {
        //given
        CardRequestDto cardRequestDto = getCardRequestDto();
        cardRequestDto.getTrunk().getAccount().setBalance(100);

        //when
        Exception expected = assertThrows(BalanceInsufficientException.class, () -> cardFacadeService.sendRequest(cardRequestDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Account balance is insufficient for application fees");
    }
}