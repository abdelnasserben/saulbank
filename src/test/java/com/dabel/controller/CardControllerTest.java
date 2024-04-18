package com.dabel.controller;

import com.dabel.DBSetupForTests;
import com.dabel.constant.Web;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

    @BeforeEach
    void setup() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldListCards() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.CARDS))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Cards");
    }

    @Test
    void shouldRedirectTo404PageWhenTryCardNotExists() throws Exception {

        mockMvc.perform(get(Web.Endpoint.CARDS + "/1"))
                .andExpect(view().name("redirect:/404"));
    }
}