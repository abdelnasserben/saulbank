package com.dabel.service.card;

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



    private CardDto getSavedCard() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());

        AccountDto savedAccount = accountService.saveAccount(AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .accountType("SAVING")
                .accountProfile("PERSONAL")
                .currency("KMF")
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

        cardFacadeService.saveCard(CardDto.builder()
                .cardName("John Doe")
                .cardNumber("1450 1525 1542 1248")
                .trunk(savedTrunk)
                .expirationDate(LocalDate.of(24, 2, 26))
                .cvc("123")
                .status("0")
                .build());
        return cardFacadeService.getAllCards().get(0);
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldActivateActiveCard() {
        //given
        //when
        cardFacadeService.activateCard(getSavedCard().getCardId());
        CardDto expected = cardFacadeService.getAllCards().get(0);

        //then
        assertThat(expected.getStatus()).isEqualTo("1");
    }

    @Test
    void shouldThrowExceptionWhenTryingActivateActiveCard() {
        //given
        CardDto savedCard = getSavedCard();
        savedCard.setStatus("1");
        cardFacadeService.saveCard(savedCard);

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> cardFacadeService.activateCard(savedCard.getCardId()));

        //then
        assertThat(expected.getMessage()).isEqualTo("Card already active");
    }

    @Test
    void shouldDeactivateCard() {
        //given
        CardDto savedCard = getSavedCard();
        savedCard.setStatus("1");
        cardFacadeService.saveCard(savedCard);

        //when
        cardFacadeService.deactivateCard(savedCard.getCardId(), "Just a reason");
        CardDto expected = cardFacadeService.getAllCards().get(0);

        //then
        assertThat(expected.getStatus()).isEqualTo("5"); //deactivated status = 5
        assertThat(expected.getFailureReason()).isEqualTo("Just a reason");
    }

    @Test
    void shouldThrowExceptionWhenTryingDeactivateInactiveCard() {
        //given
        //when
        Exception expected = assertThrows(IllegalOperationException.class,
                () -> cardFacadeService.deactivateCard(getSavedCard().getCardId(), "Just reason"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Unable to deactivate an inactive card");
    }

    @Test
    void shouldGetCustomersCardList() {
        //given
        CardDto savedCard = getSavedCard();

        //when
        List<CardDto> expected = cardFacadeService.getAllCardsByCustomer(savedCard.getTrunk().getCustomer());

        //then
        assertThat(expected.size()).isEqualTo(1);
    }
}