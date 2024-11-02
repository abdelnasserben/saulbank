package com.dabel.controller;

import com.dabel.DBSetupForTests;
import com.dabel.constant.Web;
import com.dabel.dto.BranchDto;
import com.dabel.dto.ExchangeDto;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.exchange.ExchangeFacadeService;
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
class ExchangeControllerTest {

    @Autowired
    BranchFacadeService branchFacadeService;

    @Autowired
    ExchangeFacadeService exchangeFacadeService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @Autowired
    MockMvc mockMvc;

    private void initExchange() {
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("Moroni")
                .build(), new double[3]);

        exchangeFacadeService.init(ExchangeDto.builder()
                .purchaseCurrency("EUR")
                .purchaseAmount(500)
                .saleCurrency("KMF")
                .customerIdentityNumber("NBE546646")
                .customerFullName("John Doe")
                .branch(branchFacadeService.getAll().get(0))
                .build());
    }

    @BeforeEach
    void setup() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldListExchanges() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.EXCHANGES))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Exchanges");
    }

    @Test
    void shouldDisplayInitExchangePage() throws Exception {
        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.EXCHANGE_INIT))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Init Exchange");
    }

    @Test
    void shouldNotInitAnInvalidExchange() throws Exception {

        mockMvc.perform(post(Web.Endpoint.EXCHANGE_INIT))
                .andExpect(model().attribute("errorMessage", "Invalid information !"));
    }

    @Test
    void shouldInitAValidExchange() throws Exception {
        //given
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("Moroni")
                .build(), new double[3]);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("purchaseCurrency", "EUR");
        params.add("purchaseAmount", "500");
        params.add("saleCurrency", "KMF");
        params.add("customerIdentityNumber", "NBE546646");
        params.add("customerFullName", "John Doe");

        //then
        mockMvc.perform(post(Web.Endpoint.EXCHANGE_INIT)
                .params(params)
        ).andExpect(flash().attribute("successMessage", "Exchange successfully initiated"));
    }

    @Test
    void shouldRedirectTo404PageWhenTryDisplayDetailsOfNotExistingExchange() throws Exception {

        mockMvc.perform(get(Web.Endpoint.EXCHANGES + "/1"))
                .andExpect(view().name("redirect:/404"));
    }

    @Test
    void shouldDisplayDetailsOfAnExistingExchange() throws Exception {
        //given
        initExchange();
        String url = Web.Endpoint.EXCHANGES + "/" + exchangeFacadeService.getAll().get(0).getExchangeId();

        //then
        mockMvc.perform(get(url))
                .andExpect(model().attributeExists("exchange"));
    }

    @Test
    void shouldApproveExchange() throws Exception {
        //given
        initExchange();
        String url = Web.Endpoint.EXCHANGE_APPROVE + "/" + exchangeFacadeService.getAll().get(0).getExchangeId();

        //then
        mockMvc.perform(post(url))
                .andExpect(flash().attribute("successMessage", "Exchange successfully approved!"));
    }

    @Test
    void shouldNotRejectExchangeWithoutReason() throws Exception {
        //given
        initExchange();
        String url = Web.Endpoint.EXCHANGE_REJECT + "/" + exchangeFacadeService.getAll().get(0).getExchangeId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", ""))
                .andExpect(flash().attribute("errorMessage", "Reject reason is mandatory!"));
    }

    @Test
    void shouldRejectExchange() throws Exception {
        //given
        initExchange();
        String url = Web.Endpoint.EXCHANGE_REJECT + "/" + exchangeFacadeService.getAll().get(0).getExchangeId();

        //then
        mockMvc.perform(post(url)
                        .param("rejectReason", "just a reason"))
                .andExpect(flash().attribute("successMessage", "Exchange successfully rejected!"));
    }
}