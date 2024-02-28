package com.dabel.service.exchange;

import com.dabel.DBSetupForTests;
import com.dabel.constant.Currency;
import com.dabel.constant.Status;
import com.dabel.dto.BranchDto;
import com.dabel.dto.ExchangeDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.branch.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ExchangeFacadeServiceTest {

    @Autowired
    ExchangeFacadeService exchangeFacadeService;

    @Autowired
    BranchService branchService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    private ExchangeDto getExchangeDto() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status(Status.ACTIVE.code())
                .build());
        return ExchangeDto.builder()
                .customerFullName("John Doe")
                .customerIdentityNumber("NBE56577")
                .purchaseCurrency(Currency.KMF.name())
                .purchaseAmount(25)
                .saleCurrency(Currency.EUR.name())
                .branch(savedBranch)
                .build();
    }

    @Test
    void shouldInitExchangeOfKmfToEur() {
        //given
        //when
        exchangeFacadeService.init(getExchangeDto());
        ExchangeDto expected = exchangeFacadeService.findAll().get(0);

        //then
        assertThat(expected.getExchangeId()).isGreaterThan(0);
        assertThat(expected.getStatus()).isEqualTo(Status.PENDING.code());
        assertThat(expected.getSaleAmount()).isEqualTo(0.05); //sale euro exchange course is 495.1 so 25KMF -> 0.05€
    }

    @Test
    void shouldApproveExchange() {
        //given
        exchangeFacadeService.init(getExchangeDto());
        ExchangeDto savedExchange = exchangeFacadeService.findAll().get(0);

        //when
        exchangeFacadeService.approve(savedExchange.getExchangeId());
        ExchangeDto expected = exchangeFacadeService.findAll().get(0);

        //then
        assertThat(expected.getStatus()).isEqualTo(Status.APPROVED.code());
    }

    @Test
    void shouldRejectExchange() {
        //given
        exchangeFacadeService.init(getExchangeDto());
        ExchangeDto savedExchange = exchangeFacadeService.findAll().get(0);

        //when
        exchangeFacadeService.reject(savedExchange.getExchangeId(), "Simple remarks");
        ExchangeDto expected = exchangeFacadeService.findAll().get(0);

        //then
        assertThat(expected.getStatus()).isEqualTo(Status.REJECTED.code());
        assertThat(expected.getFailureReason()).isEqualTo("Simple remarks");
    }

    @Test
    void shouldFindAllExchanges() {
        //given
        exchangeFacadeService.init(getExchangeDto());

        //when
        List<ExchangeDto> expected = exchangeFacadeService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getPurchaseAmount()).isEqualTo(25);
    }

    @Test
    void shouldFindExchangeById() {
        //given
        exchangeFacadeService.init(getExchangeDto());
        ExchangeDto savedExchange = exchangeFacadeService.findAll().get(0);

        //when
        ExchangeDto expected = exchangeFacadeService.findById(savedExchange.getExchangeId());

        //then
        assertThat(expected.getStatus()).isEqualTo(Status.PENDING.code());
        assertThat(expected.getPurchaseAmount()).isEqualTo(25);
    }

    @Test
    void shouldFindCustomerExchangesByCustomerIdentity() {
        //given
        exchangeFacadeService.init(getExchangeDto());

        //when
        List<ExchangeDto> expected = exchangeFacadeService.findAllByCustomerIdentity("NBE56577");
        List<ExchangeDto> expected2 = exchangeFacadeService.findAllByCustomerIdentity("00000");

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getPurchaseAmount()).isEqualTo(25);
        assertThat(expected2.size()).isEqualTo(0);
    }

    @Test
    void shouldThrowAnIllegalOperationExceptionWhenTryInitExchangeOfTheSameCurrency() {
        //given
        ExchangeDto exchangeDto = ExchangeDto.builder()
                .purchaseCurrency(Currency.KMF.name())
                .saleCurrency(Currency.KMF.name())
                .build();

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> exchangeFacadeService.init(exchangeDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Currencies must be different");
    }
}