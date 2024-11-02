package com.dabel.service.customer;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomerFacadeServiceTest {

    @Autowired
    CustomerFacadeService customerFacadeService;

    @Autowired
    BranchService branchService;

    @Autowired
    AccountService accountService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    private CustomerDto getCustomerDto() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .build());
        return CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE46611")
                .branch(savedBranch)
                .status("1")
                .build();
    }

    @Test
    void shouldCreateCustomerWithHisTrunk() {
        //given
        //when
        customerFacadeService.createNewCustomerWithAccount(getCustomerDto(), "John Doe", AccountType.SAVING, AccountProfile.PERSONAL);
        TrunkDto expected = accountService.findTrunkByAccountNumber(accountService.findAllAccounts().get(0).getAccountNumber());

        //then
        assertThat(expected.getAccount().getAccountName()).isEqualTo("John Doe");
        assertThat(expected.getCustomer().getIdentityNumber()).isEqualTo("NBE46611");
    }

    @Test
    void shouldUpdateCustomerInfo() {
        //given
        CustomerDto savedCustomer = customerFacadeService.saveCustomer(getCustomerDto());

        //when
        savedCustomer.setFirstName("Sarah");
        customerFacadeService.updateCustomerDetails(savedCustomer);

        //then
        assertThat(savedCustomer.getFirstName()).isEqualTo("Sarah");
    }
}