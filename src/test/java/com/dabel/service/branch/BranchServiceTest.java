package com.dabel.service.branch;

import com.dabel.DBSetupForTests;
import com.dabel.dto.BranchDto;
import com.dabel.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BranchServiceTest {

    @Autowired
    BranchService branchService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private void saveBranch() {
        branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("Moroni")
                .status("0")
                .build());
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldSaveNewBranch() {
        //given
        BranchDto branchDto = BranchDto.builder()
                .branchName("HQ")
                .branchAddress("Moroni")
                .status("0")
                .build();

        //when
        BranchDto expected = branchService.save(branchDto);

        //then
        assertThat(expected.getBranchId()).isGreaterThan(0);
    }

    @Test
    void shouldFindAllBranches() {
        //given
        saveBranch();

        //when
        List<BranchDto> expected = branchService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getBranchName()).isEqualTo("HQ");
    }

    @Test
    void shouldFindBranchById() {
        //given
        saveBranch();
        Long savedBranchId = branchService.findAll().get(0).getBranchId();

        //when
        BranchDto expected = branchService.findById(savedBranchId);

        //then
        assertThat(expected.getBranchName()).isEqualTo("HQ");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindBranchByNonExistentId() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> branchService.findById(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Branch not found");
    }
}