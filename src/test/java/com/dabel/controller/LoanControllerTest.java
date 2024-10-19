package com.dabel.controller;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Web;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.LoanDto;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import com.dabel.service.loan.LoanFacadeService;
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
class LoanControllerTest {

    @Autowired
    LoanFacadeService loanFacadeService;

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
                .identityNumber("NBE465465")
                .branch(branchFacadeService.findAll().get(0))
                .build(), "John Doe", AccountType.SAVING, AccountProfile.PERSONAL);
    }

//    private void initLoan() {
//        createCustomer();
//        loanFacadeService.init(LoanDto.builder()
//                .loanType("GOLD")
//                .borrower(customerFacadeService.findAll().get(0))
//                .interestRate(5)
//                .issuedAmount(5000)
//                .duration(6)
//                .branch(branchFacadeService.findAll().get(0))
//                .build());
//    }

    @BeforeEach
    void setup() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldListLoans() throws Exception {

        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.LOANS))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Search Loans");
    }

    @Test
    void shouldDisplayInitLoanPage() throws Exception {
        MockHttpServletResponse expected = mockMvc.perform(get(Web.Endpoint.LOAN_REQUEST))
                .andReturn()
                .getResponse();
        assertThat(expected.getContentAsString()).contains("Init Loan");
    }

    @Test
    void shouldNotInitAnInvalidLoan() throws Exception {

        mockMvc.perform(post(Web.Endpoint.LOAN_REQUEST)
                        .param("customerIdentityNumber", ""))
                .andExpect(model().attribute("errorMessage", "Invalid information !"));
    }

    @Test
    void shouldInitValidLoan() throws Exception {
        //given
        createCustomer();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("customerIdentityNumber", customerFacadeService.findAll().get(0).getIdentityNumber());
        params.add("loanType", "GOLD");
        params.add("issuedAmount", "5000");
        params.add("interestRate", "5");
        params.add("duration", "3");

        //then
        mockMvc.perform(post(Web.Endpoint.LOAN_REQUEST)
                .params(params)
        ).andExpect(flash().attribute("successMessage", "Loan successfully initiated"));
    }

    @Test
    void shouldRedirectTo404PageWhenTryDisplayDetailsOfNotExistingLoan() throws Exception {

        mockMvc.perform(get(Web.Endpoint.LOANS + "/1"))
                .andExpect(view().name("redirect:/404"));
    }

//    @Test
//    void shouldDisplayDetailsOfExistingLoan() throws Exception {
//        //given
//        initLoan();
//        String url = Web.Endpoint.LOANS + "/" + loanFacadeService.findAll().get(0).getLoanId();
//
//        //then
//        mockMvc.perform(get(url))
//                .andExpect(model().attributeExists("loan"));
//    }

//    @Test
//    void shouldApproveLoan() throws Exception {
//        //given
//        initLoan();
//        String url = Web.Endpoint.LOAN_APPROVE + "/" + loanFacadeService.findAll().get(0).getLoanId();
//
//        //then
//        mockMvc.perform(post(url))
//                .andExpect(flash().attribute("successMessage", "Loan successfully approved!"));
//    }

//    @Test
//    void shouldNotRejectLoanWithoutReason() throws Exception {
//        //given
//        initLoan();
//        String url = Web.Endpoint.LOAN_REJECT + "/" + loanFacadeService.findAll().get(0).getLoanId();
//
//        //then
//        mockMvc.perform(post(url)
//                        .param("rejectReason", ""))
//                .andExpect(flash().attribute("errorMessage", "Reject reason is mandatory!"));
//    }

//    @Test
//    void shouldRejectLoan() throws Exception {
//        //given
//        initLoan();
//        String url = Web.Endpoint.LOAN_REJECT + "/" + loanFacadeService.findAll().get(0).getLoanId();
//
//        //then
//        mockMvc.perform(post(url)
//                        .param("rejectReason", "just a reason"))
//                .andExpect(flash().attribute("successMessage", "Loan successfully rejected!"));
//    }
}