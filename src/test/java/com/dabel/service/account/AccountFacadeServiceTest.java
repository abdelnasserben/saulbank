package com.dabel.service.account;

import com.dabel.DBSetupForTests;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.service.branch.BranchService;
import com.dabel.service.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AccountFacadeServiceTest {

    @Autowired
    AccountFacadeService accountFacadeService;

    @Autowired
    BranchService branchService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    private TrunkDto saveTrunk(String accountStatus) {

        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());

        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE980125")
                .status("1")
                .branch(savedBranch)
                .build());

        AccountDto savedAccount = AccountDto.builder()
                .accountNumber("123456789")
                .accountName("John Doe")
                .currency("KMF")
                .accountType("SAVING")
                .accountProfile("PERSONAL")
                .status(accountStatus)
                .branch(savedBranch)
                .build();

        return accountFacadeService.saveTrunk(TrunkDto.builder()
                .customer(savedCustomer)
                .account(savedAccount)
                .membership("OWNER")
                .build());
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldActivateTrunkByHisId() {
        //given
        TrunkDto savedTrunk = saveTrunk("0");

        //when
        accountFacadeService.activateTrunkById(savedTrunk.getTrunkId());
        TrunkDto expected = accountFacadeService.getTrunkById(savedTrunk.getTrunkId());

        //then
        assertThat(savedTrunk.getAccount().getStatus()).isEqualTo("0");
        assertThat(expected.getAccount().getStatus()).isEqualTo("1"); //Active Status = 1
    }

    @Test
    void shouldDeactivateTrunk() {
        //given
        TrunkDto savedTrunk = saveTrunk("1");

        //when
        accountFacadeService.deactivateTrunkById(savedTrunk.getTrunkId());
        TrunkDto expected = accountFacadeService.getTrunkById(savedTrunk.getTrunkId());

        //then
        assertThat(savedTrunk.getAccount().getStatus()).isEqualTo("1");
        assertThat(expected.getAccount().getStatus()).isEqualTo("5"); //Deactivated Status = 5
    }
}