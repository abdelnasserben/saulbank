package com.dabel.service.exchange;

import com.dabel.DBSetupForTests;
import com.dabel.dto.BranchDto;
import com.dabel.dto.ExchangeDto;
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
class ExchangeServiceTest {

    @Autowired
    ExchangeService exchangeService;

    @Autowired
    BranchService branchService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private ExchangeDto getExchangeDto() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());
        return ExchangeDto.builder()
                .customerIdentityNumber("NBE465420")
                .customerFullName("John Doe")
                .purchaseAmount(500)
                .purchaseCurrency("KMF")
                .saleAmount(1.9)
                .saleCurrency("EUR")
                .status("0")
                .branch(savedBranch)
                .build();
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldSaveExchange() {
        //given
        //when
        ExchangeDto expected = exchangeService.save(getExchangeDto());

        //then
        assertThat(expected.getExchangeId()).isGreaterThan(0);
    }

    @Test
    void shouldFindAllExchanges() {
        //given
        exchangeService.save(getExchangeDto());

        //when
        List<ExchangeDto> expected = exchangeService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldFindExchangeById() {
        //given
        ExchangeDto savedExchange = exchangeService.save(getExchangeDto());

        //when
        ExchangeDto expected = exchangeService.findById(savedExchange.getExchangeId());

        //then
        assertThat(expected.getCustomerIdentityNumber()).isEqualTo("NBE465420");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindExchangeByNonExistentId() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> exchangeService.findById(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Exchange not found");
    }

    @Test
    void shouldFindCustomersExchangesByIdentityNumber() {
        //given
        exchangeService.save(getExchangeDto());

        //when
        List<ExchangeDto> expected = exchangeService.findAllByCustomerIdentity("NBE465420");

        //then
        assertThat(expected.size()).isEqualTo(1);
    }
}