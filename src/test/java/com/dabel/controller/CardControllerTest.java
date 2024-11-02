package com.dabel.controller;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Web;
import com.dabel.dto.*;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.card.CardFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    BranchFacadeService branchFacadeService;

    @Autowired
    CustomerFacadeService customerFacadeService;

    @Autowired
    AccountFacadeService accountFacadeService;

    @Autowired
    CardFacadeService cardFacadeService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @Autowired
    MockMvc mockMvc;


    private void createCustomer() {
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("Moroni")
                .build(), new double[3]);

        customerFacadeService.createNewCustomerWithAccount(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE465465")
                .branch(branchFacadeService.getAll().get(0))
                .build(), "John Doe", AccountType.SAVING, AccountProfile.PERSONAL);
    }

    private void adjustCustomerAccountBalance() {
        AccountDto accountDto = accountFacadeService.getAllTrunks().get(0).getAccount();
        accountDto.setBalance(50000);
        accountFacadeService.saveAccount(accountDto);
    }

    private void saveCardRequest() {
        createCustomer();
        adjustCustomerAccountBalance();
        cardFacadeService.initCardRequest(CardRequestDto.builder()
                .trunk(accountFacadeService.getAllTrunks().get(0))
                .cardType("VISA")
                .branch(branchFacadeService.getAll().get(0))
                .build());
    }

    private void saveCard(String status) {
        createCustomer();

        cardFacadeService.saveCard(CardDto.builder()
                .cardType("VISA")
                .cardNumber("4111111111111111")
                .cardName("John Doe")
                .expirationDate(LocalDate.of(2027, 3, 31))
                .trunk(accountFacadeService.getAllTrunks().get(0))
                .status(status)
                .cvc("123")
                .branch(branchFacadeService.getAll().get(0))
                .build());
    }

    @BeforeEach
    void setup() {
        dbSetupForTests.truncate();
    }


    /*** FOR CARD REQUESTS ***/

    @Test
    void shouldListCardRequests() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.CARD_REQUESTS))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Requests");
    }

    @Test
    void shouldNotSendAnInvalidCardRequest() throws Exception {

        mockMvc.perform(post(Web.Endpoint.CARD_REQUESTS))
                .andExpect(model().attribute("errorMessage", "Invalid request application information !"));
    }

//    @Test
//    void shouldNotSendRequestWhenAccountBalanceIsInsufficientForCardRequestFees() throws Exception {
//        //given
//        createCustomer();
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("customerIdentityNumber", customerFacadeService.findAll().get(0).getIdentityNumber());
//        params.add("accountNumber", accountFacadeService.findAllTrunks().get(0).getAccount().getAccountNumber());
//        params.add("cardType", "VISA");
//
//        //then
//        mockMvc.perform(post(Web.Endpoint.CARD_REQUESTS)
//                .params(params)
//        ).andExpect(flash().attribute("errorMessage", "Account balance is insufficient for application fees"));
//    }

    @Test
    void shouldSendValidRequest() throws Exception {
        //given
        createCustomer();
        adjustCustomerAccountBalance();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("customerIdentityNumber", customerFacadeService.getAll().get(0).getIdentityNumber());
        params.add("accountNumber", accountFacadeService.getAllTrunks().get(0).getAccount().getAccountNumber());
        params.add("cardType", "VISA");

        //then
        mockMvc.perform(post(Web.Endpoint.CARD_REQUESTS)
                .params(params)
        ).andExpect(flash().attribute("successMessage", "Card request successfully sent !"));
    }

    @Test
    void shouldRedirectTo404PageWhenTryDisplayDetailsOfNotExistingCardRequest() throws Exception {

        mockMvc.perform(get(Web.Endpoint.CARD_REQUESTS + "/1"))
                .andExpect(view().name("redirect:/404"));
    }

    @Test
    void shouldDisplayDetailsOfExistingCardRequest() throws Exception {
        //given
        saveCardRequest();
        String url = Web.Endpoint.CARD_REQUESTS + "/" + cardFacadeService.getAllCardRequests().get(0).getRequestId();

        //then
        mockMvc.perform(get(url))
                .andExpect(model().attributeExists("requestDto"));
    }

    @Test
    void shouldNotApproveCardRequestWithInvalidCardInformation() throws Exception {
        //given
        saveCardRequest();
        String url = Web.Endpoint.CARD_REQUEST_APPROVE + "/" + cardFacadeService.getAllCardRequests().get(0).getRequestId();

        //then
        mockMvc.perform(post(url))
                .andExpect(model().attribute("errorMessage", "Invalid card information. Check expiration date if no error indication has displayed"));
    }

    @Test
    void shouldApproveCardRequestWithValidCardInformation() throws Exception {
        //given
        saveCardRequest();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cardType", "VISA");
        params.add("cardNumber", "4111111111111111");
        params.add("cardName", "John Doe");
        params.add("expiryMonth", "3");
        params.add("expiryYear", "2027");
        params.add("cvc", "123");

        String url = Web.Endpoint.CARD_REQUEST_APPROVE + "/" + cardFacadeService.getAllCardRequests().get(0).getRequestId();

        //then
        mockMvc.perform(post(url)
                        .params(params))
                .andExpect(flash().attribute("successMessage", "Request approved successfully !"));
    }

    @Test
    void shouldNotRejectCardRequestWithoutReason() throws Exception {
        //given
        saveCardRequest();
        String url = Web.Endpoint.CARD_REQUEST_REJECT + "/" + cardFacadeService.getAllCardRequests().get(0).getRequestId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", ""))
                .andExpect(flash().attribute("errorMessage", "Reject reason is mandatory !"));
    }

    @Test
    void shouldRejectCardRequest() throws Exception {
        //given
        saveCardRequest();
        String url = Web.Endpoint.CARD_REQUEST_REJECT + "/" + cardFacadeService.getAllCardRequests().get(0).getRequestId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", "just a reason"))
                .andExpect(flash().attribute("successMessage", "Card request successfully rejected!"));
    }

    /*** FOR CARDS ***/

    @Test
    void shouldListCards() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.CARDS))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Cards");
    }

    @Test
    void shouldRedirectTo404PageWhenTryDisplayDetailsOfNotExistingCard() throws Exception {

        mockMvc.perform(get(Web.Endpoint.CARDS + "/1"))
                .andExpect(view().name("redirect:/404"));
    }

    @Test
    void shouldDisplayDetailsOfExistingCard() throws Exception {
        //given
        saveCard("0");
        String url = Web.Endpoint.CARDS + "/" + cardFacadeService.getAllCards().get(0).getCardId();

        //then
        mockMvc.perform(get(url))
                .andExpect(model().attributeExists("card"));
    }

    @Test
    void shouldActivateCard() throws Exception {
        //given
        saveCard("0");
        String url = Web.Endpoint.CARD_ACTIVATE + "/" + cardFacadeService.getAllCards().get(0).getCardId();

        //then
        mockMvc.perform(post(url))
                .andExpect(flash().attribute("successMessage", "Card successfully activated !"));
    }

    @Test
    void shouldNotDeactivateCardWithoutReason() throws Exception {
        //given
        saveCard("0");
        String url = Web.Endpoint.CARD_DEACTIVATE + "/" + cardFacadeService.getAllCards().get(0).getCardId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", ""))
                .andExpect(flash().attribute("errorMessage", "Deactivate reason is mandatory !"));
    }

    @Test
    void shouldDeactivateCardWithReason() throws Exception {
        //given
        saveCard("1");
        String url = Web.Endpoint.CARD_DEACTIVATE + "/" + cardFacadeService.getAllCards().get(0).getCardId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", "just a reason"))
                .andExpect(flash().attribute("successMessage", "Card successfully deactivated!"));
    }
}