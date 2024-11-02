package com.dabel.service.branch;

import com.dabel.DBSetupForTests;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.LedgerDto;
import com.dabel.service.account.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BranchFacadeServiceTest {

    @Autowired
    BranchFacadeService branchFacadeService;

    @Autowired
    AccountService accountService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private void createBranch() {
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .build(), new double[3]);
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldCreateNewBranchWithHisVaultsAndGL() {
        //given
        createBranch();

        //when
        List<AccountDto> expected = accountService.findAllAccounts();

        //then
        assertThat(expected.size()).isEqualTo(8); //because branch have 3 vaults and 5 GL
        assertThat(expected.stream()
                .filter(a -> a.getIsVault() == 1)
                .count()).isEqualTo(3);
    }

    @Test
    void shouldFindAllVaultsByBranchId() {
        //given
        createBranch();

        //when
        BranchDto savedBranch = branchFacadeService.getAll().get(0);
        List<AccountDto> expected = branchFacadeService.getAllVaultsByBranchId(savedBranch.getBranchId());

        //then
        assertThat(expected.size()).isEqualTo(3); //because branch have 3
        assertThat(expected.get(0).getIsVault()).isEqualTo(1);
        assertThat(expected.get(0).getBranch().getBranchName()).isEqualTo("HQ");
    }

    @Test
    void shouldFindAllLedgersByBranchId() {
        //given
        createBranch();

        //when
        BranchDto savedBranch = branchFacadeService.getAll().get(0);
        List<LedgerDto> expected = branchFacadeService.getAllLedgersByBranchId(savedBranch.getBranchId());

        //then
        assertThat(expected.size()).isEqualTo(5); //because branch have 5 ledgers
        assertThat(expected.get(0).getBranch().getBranchName()).isEqualTo("HQ");
    }

    @Test
    void shouldFindVaultByBranchIdAndCurrency() {
        //given
        createBranch();

        //when
        BranchDto savedBranch = branchFacadeService.getAll().get(0);
        AccountDto expected = branchFacadeService.getVaultByBranchIdAndCurrency(savedBranch.getBranchId(), "KMF");

        //then
        assertThat(expected.getCurrency()).isEqualTo("KMF");
        assertThat(expected.getIsVault()).isEqualTo(1);
        assertThat(expected.getBranch().getBranchName()).isEqualTo("HQ");
    }
}