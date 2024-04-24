package com.dabel.service.customer;

import com.dabel.DBSetupForTests;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.service.branch.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CustomerServiceTest {

    @Autowired
    CustomerService customerService;

    @Autowired
    BranchService branchService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private BranchDto getSavedBranch() {
        return branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .build());
    }

    private void saveCustomer() {
        customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE46611")
                .branch(getSavedBranch())
                .status("1")
                .build());
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldSaveNewCustomer() {
        //given
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE46611")
                .branch(getSavedBranch())
                .status("1")
                .build();

        //when
        CustomerDto expected = customerService.save(customerDto);

        //then
        assertThat(expected.getCustomerId()).isGreaterThan(0);
    }

    @Test
    void shouldFindAllCustomers() {
        //given
        saveCustomer();

        //when
        List<CustomerDto> expected = customerService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindCustomerByIdentity() {
        //given
        saveCustomer();

        //when
        CustomerDto expected = customerService.findByIdentity("NBE46611");

        //then
        assertThat(expected.getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindCustomerByNonExistentIdentity() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> customerService.findByIdentity("001357"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Customer not found");
    }



    @Test
    void shouldFindCustomerById() {
        //given
        saveCustomer();
        Long savedCustomerId = customerService.findAll().get(0).getCustomerId();

        //when
        CustomerDto expected = customerService.findById(savedCustomerId);

        //then
        assertThat(expected.getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindBranchByNonExistentId() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> customerService.findById(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Customer not found");
    }
}