package com.dabel.controller;

import com.dabel.DBSetupForTests;
import com.dabel.dto.AccountDto;
import com.dabel.dto.ChequeDto;
import com.dabel.dto.CustomerDto;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.cheque.ChequeFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppRestControllerTest {

    @Autowired
    AccountFacadeService accountFacadeService;

    @Autowired
    CustomerFacadeService customerFacadeService;

    @Autowired
    ChequeFacadeService chequeFacadeService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @Test
    void setup() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldGetCustomerByHisIdentityNumber() throws Exception {
        //given
        customerFacadeService.saveCustomer(CustomerDto.builder()
                .identityNumber("NBE46546")
                .firstName("John")
                .lastName("Doe")
                .build());

        String url = "/rest/customer/NBE46546";

        //then
        mockMvc.perform(get(url))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    void shouldRedirectToExceptionWhenRetrieveNotExistingCustomer() throws Exception {

        mockMvc.perform(get("/rest/customer/NBE46546"))
                .andExpect(status().is(302))
                .andExpect(flash().attribute("errorMessage", "Customer not found"));
    }

    @Test
    void shouldGetAccountByHisNumber() throws Exception {
        //given
        accountFacadeService.saveAccount(AccountDto.builder()
                .accountNumber("0071001997")
                .accountName("John Doe")
                .accountType("BUSINESS")
                .accountProfile("PERSONAL")
                .balance(500)
                .build());

        String url = "/rest/account/0071001997";

        //then
        mockMvc.perform(get(url))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.accountName", is("John Doe")))
                .andExpect(jsonPath("$.accountType", is("BUSINESS")))
                .andExpect(jsonPath("$.balance", is(500.0)));
    }

    @Test
    void shouldRedirectToExceptionWhenRetrieveNotExistingAccount() throws Exception {

        mockMvc.perform(get("/rest/account/0071001997"))
                .andExpect(status().is(302))
                .andExpect(flash().attribute("errorMessage", "Account not found"));
    }

    @Test
    void shouldGetChequeByHisNumber() throws Exception {
        //given
        chequeFacadeService.saveCheque(ChequeDto.builder()
                .chequeNumber("12345678")
                .status("1")
                .build());

        String url = "/rest/cheque/12345678";

        //then
        mockMvc.perform(get(url))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.status", is("1")));
    }

    @Test
    void shouldRedirectToExceptionWhenRetrieveNotExistingCheque() throws Exception {

        mockMvc.perform(get("/rest/cheque/12345678"))
                .andExpect(status().is(302))
                .andExpect(flash().attribute("errorMessage", "Cheque not found"));
    }

    @Test
    void shouldReturnBadRequestWhenTryGetCurrencyConversionBaseWithTheSameCurrency() throws Exception {

        String url = String.format("/rest/baseCurrencyInfo/%s-%s-%d", "EUR", "EUR", 500);

        mockMvc.perform(get(url))
                .andExpect(status().is(400));
    }

    @Test
    void shouldGetEurToKmfConversionBase() throws Exception {

        String url = String.format("/rest/baseCurrencyInfo/%s-%s-%d", "EUR", "KMF", 500);

        mockMvc.perform(get(url))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0]", is(490.31)))         //rate of eur -> kmf
                .andExpect(jsonPath("$[1]", is(245155.0)));    //total amount
    }

    @Test
    void shouldGetKmfToEurConversionBase() throws Exception {

        String url = String.format("/rest/baseCurrencyInfo/%s-%s-%d", "KMF", "EUR", 5000);

        mockMvc.perform(get(url))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0]", is(495.1)))         //rate of eur -> kmf
                .andExpect(jsonPath("$[1]", is(10.09)));    //total amount
    }

    @Test
    void shouldGetUSDToKmfConversionBase() throws Exception {

        String url = String.format("/rest/baseCurrencyInfo/%s-%s-%d", "USD", "KMF", 500);

        mockMvc.perform(get(url))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0]", is(456.51)))         //rate of eur -> kmf
                .andExpect(jsonPath("$[1]", is(228255.0)));    //total amount
    }

    @Test
    void shouldGetKmfToUsdConversionBase() throws Exception {

        String url = String.format("/rest/baseCurrencyInfo/%s-%s-%d", "KMF", "USD", 5000);

        mockMvc.perform(get(url))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0]", is(462.12)))         //rate of eur -> kmf
                .andExpect(jsonPath("$[1]", is(10.81)));    //total amount
    }

    @Test
    void shouldGetLoanTotalDueAmount() throws Exception {

        String url = String.format("/rest/loanTotalDue/%d-%d", 5000, 5);

        mockMvc.perform(get(url))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", is(5250.0)));
    }
}