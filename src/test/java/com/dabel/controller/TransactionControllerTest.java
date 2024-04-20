package com.dabel.controller;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Web;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TransactionDto;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import com.dabel.service.transaction.TransactionFacadeService;
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
class TransactionControllerTest {

    @Autowired
    BranchFacadeService branchFacadeService;

    @Autowired
    TransactionFacadeService transactionFacadeService;

    @Autowired
    CustomerFacadeService customerFacadeService;

    @Autowired
    AccountFacadeService accountFacadeService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @Autowired
    MockMvc mockMvc;

    private void saveCustomerAccount() {
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("Moroni")
                .build(), new double[3]);

        customerFacadeService.create(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE547978")
                .branch(branchFacadeService.findAll().get(0))
                .build(), "John Doe", AccountType.SAVING, AccountProfile.PERSONAL);
    }

    private void initTransaction() {
        saveCustomerAccount();

        transactionFacadeService.init(TransactionDto.builder()
                .transactionType("DEPOSIT")
                .initiatorAccount(accountFacadeService.findAllTrunks().get(0).getAccount())
                .currency("KMF")
                .amount(500)
                .customerIdentity("NBE547978")
                .customerFullName("John Doe")
                .branch(branchFacadeService.findAll().get(0))
                .build());
    }

    @BeforeEach
    void setup() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldListTransactions() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.TRANSACTIONS))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Transactions");
    }

    @Test
    void shouldDisplayInitTransactionPage() throws Exception {
        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.TRANSACTION_INIT))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Init Transaction");
    }

    @Test
    void shouldNotInitAnInvalidTransaction() throws Exception {
        //given
        saveCustomerAccount();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("transactionType", "DEPOSIT");
        params.add("initiatorAccountNumber", accountFacadeService.findAllTrunks().get(0).getAccount().getAccountNumber());
        params.add("currency", "KMF");

        //then
        mockMvc.perform(post(Web.Endpoint.TRANSACTION_INIT)
                        .params(params))
                .andExpect(model().attribute("errorMessage", "Invalid information!"));
    }

    @Test
    void shouldInitValidTransaction() throws Exception {
        //given
        saveCustomerAccount();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("transactionType", "DEPOSIT");
        params.add("initiatorAccountNumber", accountFacadeService.findAllTrunks().get(0).getAccount().getAccountNumber());
        params.add("currency", "KMF");
        params.add("amount", "500");
        params.add("customerIdentity", "NBE547978");
        params.add("customerFullName", "John Doe");

        //then
        mockMvc.perform(post(Web.Endpoint.TRANSACTION_INIT)
                        .params(params))
                .andExpect(flash().attribute("successMessage", "DEPOSIT successfully initiated."));
    }

    @Test
    void shouldInitValidTransfer() throws Exception {
        //given
        saveCustomerAccount();

        customerFacadeService.create(CustomerDto.builder()
                .firstName("Mark")
                .lastName("Patrick")
                .branch(branchFacadeService.findAll().get(0))
                .build(), "Mark Patrick", AccountType.SAVING, AccountProfile.PERSONAL);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("transactionType", "TRANSFER");
        params.add("initiatorAccountNumber", accountFacadeService.findAllTrunks().get(0).getAccount().getAccountNumber());
        params.add("receiverAccountNumber", accountFacadeService.findAllTrunks().get(1).getAccount().getAccountNumber());
        params.add("currency", "KMF");
        params.add("amount", "500");
        params.add("customerIdentity", "NBE547978");
        params.add("customerFullName", "John Doe");

        //then
        mockMvc.perform(post(Web.Endpoint.TRANSACTION_INIT)
                        .params(params))
                .andExpect(flash().attribute("errorMessage", "Account balance is insufficient"));
    }

    @Test
    void shouldRedirectTo404PageWhenTryDisplayDetailsOfNotExistingTransaction() throws Exception {

        mockMvc.perform(get(Web.Endpoint.TRANSACTIONS + "/1"))
                .andExpect(view().name("redirect:/404"));
    }

    @Test
    void shouldDisplayDetailsOfAnExistingTransaction() throws Exception {
        //given
        initTransaction();
        String url = Web.Endpoint.TRANSACTIONS + "/" + transactionFacadeService.findAll().get(0).getTransactionId();

        //then
        mockMvc.perform(get(url))
                .andExpect(model().attributeExists("transaction"));
    }

    @Test
    void shouldApproveTransaction() throws Exception {
        //given
        initTransaction();
        String url = Web.Endpoint.TRANSACTION_APPROVE + "/" + transactionFacadeService.findAll().get(0).getTransactionId();

        //then
        mockMvc.perform(post(url))
                .andExpect(flash().attribute("successMessage", "Transaction successfully approved!"));
    }

    @Test
    void shouldNotRejectTransactionWithoutReason() throws Exception {
        //given
        initTransaction();
        String url = Web.Endpoint.TRANSACTION_REJECT + "/" + transactionFacadeService.findAll().get(0).getTransactionId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", ""))
                .andExpect(flash().attribute("errorMessage", "Reject reason is mandatory!"));
    }

    @Test
    void shouldRejectTransaction() throws Exception {
        //given
        initTransaction();
        String url = Web.Endpoint.TRANSACTION_REJECT + "/" + transactionFacadeService.findAll().get(0).getTransactionId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", "just a reason"))
                .andExpect(flash().attribute("successMessage", "Transaction successfully rejected!"));
    }
}