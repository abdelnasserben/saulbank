package com.dabel.controller;

import com.dabel.DBSetupForTests;
import com.dabel.constant.Web;
import com.dabel.dto.BranchDto;
import com.dabel.service.branch.BranchFacadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
class View360ControllerTest {

    @Autowired
    BranchFacadeService branchFacadeService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @Autowired
    MockMvc mockMvc;


    private void createBranch(double[] vaultsAsset) {
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("Moroni")
                .build(), vaultsAsset);
    }

    @BeforeEach
    void setup() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldListBranches() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.VIEW360_BRANCHES))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Branches");
    }

    @Test
    void shouldNotCreateAnInvalidBranch() throws Exception {

        mockMvc.perform(post(Web.Endpoint.VIEW360_BRANCHES))
                .andExpect(model().attribute("errorMessage", "Invalid information !"));
    }
    @Test
    void shouldCreateValidBranch() throws Exception {
        //given
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("branchName", "HQ");
        params.add("branchAddress", "Moroni");

        //then
        mockMvc.perform(post(Web.Endpoint.VIEW360_BRANCHES)
                 .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                 .params(params)
                ).andExpect(flash().attribute("successMessage", "New branch added successfully !"));

    }

    @Test
    void shouldListVaultsWithoutBranchCode() throws Exception {

        mockMvc.perform(get(Web.Endpoint.VIEW360_VAULT_GL))
                .andExpect(model().attributeDoesNotExist("branch"));
    }

    @Test
    void shouldIndicateBranchNotFoundWhenTryListVaultsWIthIncorrectCode() throws Exception {

        mockMvc.perform(get(Web.Endpoint.VIEW360_VAULT_GL)
                        .param("code", "12"))
                .andExpect(model().attribute("errorMessage", "Branch not found"));
    }

    @Test
    void shouldListVaultsWithBranchCode() throws Exception {
        //given
        createBranch(new double[3]);

        //then
        mockMvc.perform(get(Web.Endpoint.VIEW360_VAULT_GL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("code", String.valueOf(branchFacadeService.getAll().get(0).getBranchId())))
                .andExpect(model().attributeExists("vaults"))
                .andExpect(model().attributeExists("ledgers"));
    }

    @Test
    void shouldNotAdjustVaultWithNegativeAmount() throws Exception {
        //given
        createBranch(new double[]{100, 200, 300});

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", String.valueOf(branchFacadeService.getAll().get(0).getBranchId()));
        params.add("currency", "KMF");
        params.add("amount", "-500");
        params.add("operationType", "credit");

        //then
        mockMvc.perform(post(Web.Endpoint.VIEW360_VAULT_GL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .params(params)
        ).andExpect(flash().attribute("errorMessage", "Amount must be positive !"));

    }

    @Test
    void shouldCreditVault() throws Exception {
        //given
        createBranch(new double[]{100, 200, 300});

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", String.valueOf(branchFacadeService.getAll().get(0).getBranchId()));
        params.add("currency", "KMF");
        params.add("amount", "500");
        params.add("operationType", "credit");

        //then
        mockMvc.perform(post(Web.Endpoint.VIEW360_VAULT_GL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .params(params)
        ).andExpect(flash().attribute("successMessage", "Successful adjustment"));

    }

    @Test
    void shouldDebitVault() throws Exception {
        //given
        createBranch(new double[]{100, 200, 300});

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", String.valueOf(branchFacadeService.getAll().get(0).getBranchId()));
        params.add("currency", "KMF");
        params.add("amount", "500");
        params.add("operationType", "debit");

        //then
        mockMvc.perform(post(Web.Endpoint.VIEW360_VAULT_GL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .params(params)
        ).andExpect(flash().attribute("successMessage", "Successful adjustment"));

    }
}