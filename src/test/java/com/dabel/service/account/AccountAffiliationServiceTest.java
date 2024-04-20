package com.dabel.service.account;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.service.branch.BranchService;
import com.dabel.service.customer.CustomerFacadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AccountAffiliationServiceTest {

    @Autowired
    AccountAffiliationService accountAffiliationService;

    @Autowired
    CustomerFacadeService customerFacadeService;

    @Autowired
    AccountService accountService;

    @Autowired
    BranchService branchService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    BranchDto savedBranch;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
        savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status(Status.ACTIVE.code())
                .build());
    }

    @Test
    void shouldAddNewCustomerAsJointedOnPersonalAccount() {
        //given
        CustomerDto ownerCustomer = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("US4546888")
                .branch(savedBranch)
                .build();

        CustomerDto newCustomer = CustomerDto.builder()
                .firstName("Sarah")
                .lastName("Hunt")
                .identityNumber("NBE123456")
                .branch(savedBranch)
                .build();
        customerFacadeService.create(ownerCustomer, "John Doe", AccountType.SAVING, AccountProfile.PERSONAL);
        String savedAccountNumber = accountService.findAll().get(0).getAccountNumber();

        //when
        accountAffiliationService.add(newCustomer, savedAccountNumber);
        CustomerDto savedCustomer = customerFacadeService.findByIdentity("NBE123456");
        TrunkDto expected = accountService.findTrunkByCustomerAndAccountNumber(savedCustomer, savedAccountNumber);

        //then
        assertThat(expected.getAccount().getAccountProfile()).isEqualTo("JOINT");
        assertThat(expected.getMembership()).isEqualTo("JOINTED");
    }

    @Test
    void shouldAddExistsCustomerAsJointedOnPersonalAccount() {
        //given
        CustomerDto customer1 = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("US4546888")
                .branch(savedBranch)
                .build();

        CustomerDto customer2 = CustomerDto.builder()
                .firstName("Sarah")
                .lastName("Hunt")
                .identityNumber("NBE123456")
                .branch(savedBranch)
                .build();
        customerFacadeService.create(customer1, "John Doe", AccountType.SAVING, AccountProfile.PERSONAL);
        customerFacadeService.create(customer2, "Sarah Hunt", AccountType.BUSINESS, AccountProfile.PERSONAL);

        AccountDto savedAccount1 = accountService.findAll().get(0);
        CustomerDto savedCustomer2 = customerFacadeService.findAll().get(1);

        //when
        accountAffiliationService.add(customer2, savedAccount1.getAccountNumber());
        List<TrunkDto> expected = accountService.findAllTrunks(savedCustomer2);

        //then
        assertThat(expected.size()).isEqualTo(2);
        assertThat(expected.get(0).getAccount().getAccountProfile()).isEqualTo("PERSONAL");
        assertThat(expected.get(0).getMembership()).isEqualTo("OWNER");
//        assertThat(expected.get(1).getAccount().getAccountProfile()).isEqualTo("JOINT");
        assertThat(expected.get(0).getMembership()).isEqualTo("JOINTED");
    }

    @Test
    void remove() {
    }
}