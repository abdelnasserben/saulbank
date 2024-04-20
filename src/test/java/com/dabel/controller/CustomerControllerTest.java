package com.dabel.controller;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Web;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    BranchFacadeService branchFacadeService;

    @Autowired
    CustomerFacadeService customerFacadeService;

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
                .identityNumber("NBE465464")
                .branch(branchFacadeService.findAll().get(0))
                .build(), "John Doe", AccountType.SAVING, AccountProfile.PERSONAL);
    }

    @BeforeEach
    void setup() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldListCustomers() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.CUSTOMERS))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Customers");
    }

    @Test
    void shouldDisplayAddCustomerPage() throws Exception {
        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.CUSTOMER_ADD))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("New Customer");
    }

    @Test
    void shouldCreateValidCustomer() throws Exception {
        //given
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("Moroni")
                .build(), new double[3]);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("firstName", "John");
        params.add("lastName", "Doe");
        params.add("identityNumber", "NBE465464");
        params.add("accountName", "John Doe");
        params.add("accountType", "SAVING");
        params.add("accountProfile", "PERSONAL");

        byte[] multipartContent = "content of file".getBytes(StandardCharsets.UTF_8);

        //then
        mockMvc.perform(multipart(Web.Endpoint.CUSTOMER_ADD)
                        .file(new MockMultipartFile("avatar", "avatar.txt", "text/plain", multipartContent))
                        .file(new MockMultipartFile("signature", "signature.txt", "text/plain", multipartContent))
                        .params(params))
                .andExpect(flash().attribute("successMessage", "Customer added successfully !"));
    }

    @Test
    void shouldRedirectTo404PageWhenTryDisplayDetailsOfNotExistingCustomer() throws Exception {

        mockMvc.perform(get(Web.Endpoint.CUSTOMERS + "/1"))
                .andExpect(view().name("redirect:/404"));
    }

    @Test
    void shouldDisplayDetailsOfExistingCustomer() throws Exception {
        //given
        createCustomer();
        String url = Web.Endpoint.CUSTOMERS + "/" + customerFacadeService.findAll().get(0).getCustomerId();

        //then
        mockMvc.perform(get(url))
                .andExpect(model().attributeExists("customer"));
    }

    @Test
    void shouldUpdateCustomerInfo() throws Exception {
        //given
        createCustomer();
        String url = Web.Endpoint.CUSTOMERS + "/" + customerFacadeService.findAll().get(0).getCustomerId();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("firstName", "Sarah");
        params.add("lastName", "Hunt");
        params.add("identityNumber", "NBE465464");

        //then
        mockMvc.perform(post(url)
                        .params(params))
                .andExpect(flash().attribute("successMessage", "Customer information updated successfully !"));
    }

    @Test
    void shouldNotUpdateAnInvalidCustomer() throws Exception {
        //given
        createCustomer();
        String url = Web.Endpoint.CUSTOMERS + "/" + customerFacadeService.findAll().get(0).getCustomerId();

        //then
        mockMvc.perform(post(url))
                .andExpect(flash().attribute("errorMessage", "Invalid information !"));
    }
}