package com.dabel.service.customer;

import com.dabel.DBSetupForTests;
import com.dabel.constant.Status;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                .status(Status.ACTIVE.code())
                .build();
    }

    @Test
    void shouldCreateCustomerWithHisTrunk() {
        //given
        //when
        customerFacadeService.create(getCustomerDto());
        TrunkDto expected = accountService.findTrunkByNumber(accountService.findAll().get(0).getAccountNumber());

        //then
        assertThat(expected.getAccount().getAccountName()).isEqualTo("John Doe");
        assertThat(expected.getCustomer().getIdentityNumber()).isEqualTo("NBE46611");
    }

    @Test
    void shouldFindAllCustomers() {
        //given
        customerFacadeService.create(getCustomerDto());

        //when
        List<CustomerDto> expected = customerFacadeService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindCustomerByIdentityNumber() {
        //given
        customerFacadeService.create(getCustomerDto());

        //when
        CustomerDto expected = customerFacadeService.findByIdentity("NBE46611");

        //then
        assertThat(expected.getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsCustomer() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> customerFacadeService.findByIdentity("0123456"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Customer not found");
    }
}