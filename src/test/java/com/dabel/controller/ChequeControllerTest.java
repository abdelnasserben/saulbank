package com.dabel.controller;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Web;
import com.dabel.dto.*;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.cheque.ChequeFacadeService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ChequeControllerTest {

    @Autowired
    BranchFacadeService branchFacadeService;

    @Autowired
    CustomerFacadeService customerFacadeService;

    @Autowired
    AccountFacadeService accountFacadeService;

    @Autowired
    ChequeFacadeService chequeFacadeService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @Autowired
    MockMvc mockMvc;


    private void createCustomer() {
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("Moroni")
                .build(), new double[3]);

        customerFacadeService.create(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE465465")
                .branch(branchFacadeService.findAll().get(0))
                .build(), "John Doe", AccountType.SAVING, AccountProfile.PERSONAL);
    }

    private void adjustCustomerAccountBalance() {
        AccountDto accountDto = accountFacadeService.findAllTrunks().get(0).getAccount();
        accountDto.setBalance(50000);
        accountFacadeService.save(accountDto);
    }

    private void saveChequeRequest() {
        createCustomer();
        adjustCustomerAccountBalance();

        chequeFacadeService.sendRequest(PostChequeRequestDto.builder()
                .accountNumber(accountFacadeService.findAllTrunks().get(0).getAccount().getAccountNumber())
                .customerIdentityNumber("NBE465465")
                .build());
    }

    private void saveCheque(String status) {
        saveChequeRequest();

        chequeFacadeService.saveCheque(ChequeDto.builder()
                .chequeNumber("12345678")
                .serial(chequeFacadeService.findAllRequests().get(0))
                .trunk(accountFacadeService.findAllTrunks().get(0))
                .status(status)
                .branch(branchFacadeService.findAll().get(0))
                .build());
    }

    @BeforeEach
    void setup() {
        dbSetupForTests.truncate();
    }


    /*** FOR CHEQUE REQUESTS ***/

    @Test
    void shouldListChequeRequests() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.CHEQUE_REQUESTS))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Requests");
    }

    @Test
    void shouldNotSendAnInvalidChequeRequest() throws Exception {

        mockMvc.perform(post(Web.Endpoint.CHEQUE_REQUESTS))
                .andExpect(model().attribute("errorMessage", "Invalid request application information !"));
    }

    @Test
    void shouldSendValidRequest() throws Exception {
        //given
        createCustomer();
        adjustCustomerAccountBalance();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("accountNumber", accountFacadeService.findAllTrunks().get(0).getAccount().getAccountNumber());
        params.add("customerIdentityNumber", customerFacadeService.findAll().get(0).getIdentityNumber());

        //then
        mockMvc.perform(post(Web.Endpoint.CHEQUE_REQUESTS)
                .params(params)
        ).andExpect(flash().attribute("successMessage", "Cheque request successfully sent !"));
    }

    @Test
    void shouldRedirectTo404PageWhenTryDisplayDetailsOfNotExistingChequeRequest() throws Exception {

        mockMvc.perform(get(Web.Endpoint.CHEQUE_REQUESTS + "/1"))
                .andExpect(view().name("redirect:/404"));
    }

    @Test
    void shouldDisplayDetailsOfExistingChequeRequest() throws Exception {
        //given
        saveChequeRequest();
        String url = Web.Endpoint.CHEQUE_REQUESTS + "/" + chequeFacadeService.findAllRequests().get(0).getRequestId();

        //then
        mockMvc.perform(get(url))
                .andExpect(model().attributeExists("requestDto"));
    }

    @Test
    void shouldApproveChequeRequest() throws Exception {
        //given
        saveChequeRequest();
        adjustCustomerAccountBalance();

        String url = Web.Endpoint.CHEQUE_REQUEST_APPROVE + "/" + chequeFacadeService.findAllRequests().get(0).getRequestId();

        //then
        mockMvc.perform(post(url))
                .andExpect(flash().attribute("successMessage", "Cheque request successfully approved!"));
    }

    @Test
    void shouldNotRejectChequeRequestWithoutReason() throws Exception {
        //given
        saveChequeRequest();
        String url = Web.Endpoint.CHEQUE_REQUEST_REJECT + "/" + chequeFacadeService.findAllRequests().get(0).getRequestId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", ""))
                .andExpect(flash().attribute("errorMessage", "Reject reason is mandatory!"));
    }

    @Test
    void shouldRejectChequeRequest() throws Exception {
        //given
        saveChequeRequest();
        String url = Web.Endpoint.CHEQUE_REQUEST_REJECT + "/" + chequeFacadeService.findAllRequests().get(0).getRequestId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", "just a reason"))
                .andExpect(flash().attribute("successMessage", "Cheque request successfully rejected!"));
    }


    /*** FOR CHEQUES ***/

    @Test
    void shouldListCheques() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.CHEQUES))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Cheques");
    }

    @Test
    void shouldRedirectTo404PageWhenTryDisplayDetailsOfNotExistingCheque() throws Exception {

        mockMvc.perform(get(Web.Endpoint.CHEQUES + "/1"))
                .andExpect(view().name("redirect:/404"));
    }

    @Test
    void shouldDisplayDetailsOfExistingCheque() throws Exception {
        //given
        saveCheque("0");
        String url = Web.Endpoint.CHEQUES + "/" + chequeFacadeService.findAllCheques().get(0).getChequeId();

        //then
        mockMvc.perform(get(url))
                .andExpect(model().attributeExists("cheque"));
    }

    @Test
    void shouldActivateCheque() throws Exception {
        //given
        saveCheque("0");
        String url = Web.Endpoint.CHEQUE_ACTIVATE + "/" + chequeFacadeService.findAllCheques().get(0).getChequeId();

        //then
        mockMvc.perform(post(url))
                .andExpect(flash().attribute("successMessage", "Cheque successfully activated !"));
    }

    @Test
    void shouldNotDeactivateChequeWithoutReason() throws Exception {
        //given
        saveCheque("0");
        String url = Web.Endpoint.CHEQUE_DEACTIVATE + "/" + chequeFacadeService.findAllCheques().get(0).getChequeId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", ""))
                .andExpect(flash().attribute("errorMessage", "Deactivate reason is mandatory !"));
    }

    @Test
    void shouldDeactivateChequeWithReason() throws Exception {
        //given
        saveCheque("1");
        String url = Web.Endpoint.CHEQUE_DEACTIVATE + "/" + chequeFacadeService.findAllCheques().get(0).getChequeId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", "just a reason"))
                .andExpect(flash().attribute("successMessage", "Cheque successfully deactivated!"));
    }


    /*** FOR CHEQUES PAYMENTS ***/


    @Test
    void shouldDisplayChequePaymentPage() throws Exception {
        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.CHEQUE_PAY))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Pay A Cheque");
    }

    @Test
    void shouldNotInitChequePaymentWithInvalidInformation() throws Exception {
        //given
        createCustomer();

        //then
        mockMvc.perform(post(Web.Endpoint.CHEQUE_PAY))
                .andExpect(model().attribute("errorMessage", "Invalid information!"));
    }

    @Test
    void shouldInitValidChequePayment() throws Exception {
        //given
        saveCheque("1");

        customerFacadeService.create(CustomerDto.builder()
                .firstName("Sarah")
                .lastName("Hunt")
                .identityNumber("NBE021465")
                .branch(branchFacadeService.findAll().get(0))
                .build(), "Sarah Hunt", AccountType.SAVING, AccountProfile.PERSONAL);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chequeNumber", chequeFacadeService.findAllCheques().get(0).getChequeNumber());
        params.add("beneficiaryAccountNumber", accountFacadeService.findAllTrunks().get(1).getAccount().getAccountNumber());
        params.add("amount", "500");

        //then
        mockMvc.perform(post(Web.Endpoint.CHEQUE_PAY)
                        .params(params))
                .andExpect(flash().attribute("successMessage", "Cheque payment successfully initiated!"));
    }

}