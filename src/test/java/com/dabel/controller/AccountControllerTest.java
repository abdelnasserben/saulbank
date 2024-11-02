package com.dabel.controller;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Web;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.branch.BranchFacadeService;
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
class AccountControllerTest {

    @Autowired
    BranchFacadeService branchFacadeService;

    @Autowired
    CustomerFacadeService customerFacadeService;

    @Autowired
    AccountFacadeService accountFacadeService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @Autowired
    MockMvc mockMvc;


    private void createCustomerAccount() {
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

    private void saveInactiveTrunk() {
        createCustomerAccount();
        accountFacadeService.deactivateTrunkById(accountFacadeService.getAllTrunks().get(0).getTrunkId());
    }

    @BeforeEach
    void setup() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldListTrunks() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.ACCOUNTS))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Accounts");
    }

    @Test
    void shouldRedirectTo404PageWhenTryDisplayDetailsOfNotExistingTrunk() throws Exception {

        mockMvc.perform(get(Web.Endpoint.ACCOUNTS + "/1"))
                .andExpect(view().name("redirect:/404"));
    }

    @Test
    void shouldDisplayDetailsOfExistingTrunk() throws Exception {
        //given
        createCustomerAccount();
        String url = Web.Endpoint.ACCOUNTS + "/" + accountFacadeService.getAllTrunks().get(0).getTrunkId();

        //then
        mockMvc.perform(get(url))
                .andExpect(view().name(Web.View.ACCOUNT_DETAILS))
                .andExpect(model().attributeExists("trunk"));
    }

    @Test
    void shouldActivateTrunk() throws Exception {
        //given
        saveInactiveTrunk();
        String url = Web.Endpoint.ACCOUNT_ACTIVATE + "/" + accountFacadeService.getAllTrunks().get(0).getTrunkId();

        //then
        mockMvc.perform(post(url))
                .andExpect(flash().attribute("successMessage", "Account successfully activated !"));
    }

    @Test
    void shouldDeactivateTrunk() throws Exception {
        //given
        saveInactiveTrunk();
        String url = Web.Endpoint.ACCOUNT_DEACTIVATE + "/" + accountFacadeService.getAllTrunks().get(0).getTrunkId();

        //then
        mockMvc.perform(post(url))
                .andExpect(flash().attribute("successMessage", "Account successfully deactivated!"));
    }

    @Test
    void shouldDisplayTrunkAffiliationsPageWithoutCode() throws Exception {

        mockMvc.perform(get(Web.Endpoint.ACCOUNT_AFFILIATION))
                .andExpect(view().name(Web.View.ACCOUNT_AFFILIATION))
                .andExpect(model().attributeDoesNotExist("account"));
    }

    @Test
    void shouldNotifyAccountNotFoundWhenTryDisplayTrunkAffiliationsPageWithNotExistingCode() throws Exception {

        mockMvc.perform(get(Web.Endpoint.ACCOUNT_AFFILIATION)
                        .param("code", "fakeNumber"))
                .andExpect(view().name(Web.View.ACCOUNT_AFFILIATION))
                .andExpect(model().attribute("errorMessage", "Account not found"));
    }

    @Test
    void shouldDisplayTrunkAffiliationsPageWithCode() throws Exception {
        //given
        createCustomerAccount();

        //then
        mockMvc.perform(get(Web.Endpoint.ACCOUNT_AFFILIATION)
                        .param("code", accountFacadeService.getAllTrunks().get(0).getAccount().getAccountNumber()))
                .andExpect(view().name(Web.View.ACCOUNT_AFFILIATION))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    void shouldDisplayFormPageOfTrunkAffiliationManagementWithoutMember() throws Exception {
        //given
        createCustomerAccount();
        String url = Web.Endpoint.ACCOUNT_AFFILIATION + "/" + accountFacadeService.getAllTrunks().get(0).getAccount().getAccountNumber();

        //then
        mockMvc.perform(get(url))
                .andExpect(view().name(Web.View.ACCOUNT_AFFILIATION_ADD))
                .andExpect(model().attribute("customer", new CustomerDto()))
                .andExpect(model().attributeExists("trunk"));
    }

    @Test
    void shouldDisplayFormPageOfTrunkAffiliationManagementWithMember() throws Exception {
        //given
        createCustomerAccount();
        String url = Web.Endpoint.ACCOUNT_AFFILIATION + "/" + accountFacadeService.getAllTrunks().get(0).getAccount().getAccountNumber();

        //then
        mockMvc.perform(get(url)
                        .param("member", "NBE465465"))
                .andExpect(view().name(Web.View.ACCOUNT_AFFILIATION_ADD))
                .andExpect(model().attribute("customer", customerFacadeService.getAll().get(0)))
                .andExpect(model().attributeExists("trunk"));
    }

    @Test
    void shouldNotAffiliateNotExistingCustomerWithInvalidInformation() throws Exception {
        //given
        createCustomerAccount();
        String url = Web.Endpoint.ACCOUNT_AFFILIATION + "/" + accountFacadeService.getAllTrunks().get(0).getAccount().getAccountNumber();

        //then
        mockMvc.perform(post(url)
                        .param("member", ""))
                .andExpect(flash().attribute("errorMessage", "Invalid Information!"));
    }

    @Test
    void shouldAffiliateNotExistingCustomerAsAffiliate() throws Exception {
        //given
        createCustomerAccount();
        String url = Web.Endpoint.ACCOUNT_AFFILIATION + "/" + accountFacadeService.getAllTrunks().get(0).getAccount().getAccountNumber();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("member", "");
        params.add("firstName", "Sarah");
        params.add("lastName", "Hunt");
        params.add("identityNumber", "NBE021465");

        //then
        mockMvc.perform(post(url)
                        .params(params))
                .andExpect(flash().attribute("successMessage", "Successful affiliation!"));
    }

    @Test
    void shouldAffiliateExistingCustomer() throws Exception {
        //given
        createCustomerAccount();

        customerFacadeService.createNewCustomerWithAccount(CustomerDto.builder()
                .firstName("Sarah")
                .lastName("Hunt")
                .identityNumber("NBE021465")
                .branch(branchFacadeService.getAll().get(0))
                .build(), "Sarah Hunt", AccountType.SAVING, AccountProfile.PERSONAL);

        String url = Web.Endpoint.ACCOUNT_AFFILIATION + "/" + accountFacadeService.getAllTrunks().get(0).getAccount().getAccountNumber();

        //then
        mockMvc.perform(post(url)
                        .param("member", "NBE021465"))
                .andExpect(flash().attribute("successMessage", "Successful affiliation!"));
    }

    @Test
    void shouldRemoveAffiliate() throws Exception {
        //given
        createCustomerAccount();

        CustomerDto savedNextAffiliate = CustomerDto.builder()
                .firstName("Sarah")
                .lastName("Hunt")
                .identityNumber("NBE021465")
                .branch(branchFacadeService.getAll().get(0))
                .build();
        accountFacadeService.addAffiliateToAccount(savedNextAffiliate, accountFacadeService.getAllTrunks().get(0).getAccount().getAccountNumber());

        String url = String.format("%s/%s/remove/%s", Web.Endpoint.ACCOUNT_AFFILIATION, accountFacadeService.getAllTrunks().get(0).getTrunkId(), "NBE021465");

        //then
        mockMvc.perform(post(url))
                .andExpect(flash().attribute("successMessage", "Affiliate removed successfully!"));
    }
}