package com.dabel.service.branch;

import com.dabel.DBSetupForTests;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.service.account.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BranchFacadeServiceTest {

    @Autowired
    BranchFacadeService branchFacadeService;

    @Autowired
    AccountService accountService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldCreateNewBranchWithHisVaultsAndGL() {
        //given
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .build(), new double[3]);

        //when
        List<AccountDto> expected = accountService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(8);
        assertThat(expected.stream()
                .filter(a -> a.getIsVault() == 1)
                .count()).isEqualTo(3);
    }

    @Test
    void shouldFindAllBranches() {
        //given
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .build(),  new double[3]);

        //then
        List<BranchDto> expected = branchFacadeService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getBranchName()).isEqualTo("HQ");
    }

    @Test
    void shouldFindBranchById() {
        //given
        branchFacadeService.create(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .build(),  new double[3]);

        //when
        BranchDto expected = branchFacadeService.findById(1L);

        //then
        assertThat(expected.getBranchName()).isEqualTo("HQ");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTryFindNotExistsBranch() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> branchFacadeService.findById(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Branch not found");
    }
}