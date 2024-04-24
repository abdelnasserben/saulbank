package com.dabel.service.exchange;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.ExchangeDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ExchangeOperationServiceTest {

    @Autowired
    ExchangeOperationService exchangeOperationService;

    @Autowired
    AccountService accountService;

    @Autowired
    BranchService branchService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    private BranchDto getSavedBranch() {
        return branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());
    }

    private ExchangeDto getExchangeDto(String currency1, String currency2) {
        return ExchangeDto.builder()
                .customerIdentityNumber("NBE465420")
                .customerFullName("John Doe")
                .purchaseAmount(500)
                .purchaseCurrency(currency1)
                .saleCurrency(currency2)
                .branch(getSavedBranch())
                .build();
    }

    //TODO: an exchange requires two Vaults of the two currencies involved
    private void createVaults(ExchangeDto exchangeDto) {
        accountService.save(AccountDto.builder()
                .accountName("Vault Code 1")
                .accountNumber("987654321")
                .currency(exchangeDto.getPurchaseCurrency())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .status("1")
                .isVault(1)
                .branch(exchangeDto.getBranch())
                .build());

        accountService.save(AccountDto.builder()
                .accountName("Vault Code 1")
                .accountNumber("123456789")
                .currency(exchangeDto.getSaleCurrency())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .status("1")
                .isVault(1)
                .branch(exchangeDto.getBranch())
                .build());
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldGetExchangeService() {
        assertThat(exchangeOperationService.getExchangeService()).isNotNull();
    }

    @Test
    void shouldInitiateKmfToEurExchange() {
        //given
        ExchangeDto exchangeDto = getExchangeDto("KMF", "EUR");

        //when
        exchangeOperationService.init(exchangeDto);
        ExchangeDto expected = exchangeOperationService.getExchangeService().findAll().get(0);

        //then
        assertThat(expected.getExchangeId()).isGreaterThan(0);
    }

    @Test
    void shouldThrowExceptionWhenInitiatingExchangeThatDoesNotIncludeKmfCurrency() {
        //given
        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> exchangeOperationService.init(getExchangeDto("EUR", "USD")));

        //then
        assertThat(expected.getMessage()).isEqualTo("An exchange must involve KMF currency");
    }

    @Test
    void shouldThrowExceptionWhenInitiatingExchangeWithSameCurrencies() {
        //given
        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> exchangeOperationService.init(getExchangeDto("KMF", "KMF")));

        //then
        assertThat(expected.getMessage()).isEqualTo("Currencies must be different");
    }

    @Test
    void shouldApproveEurToKmfExchange() {
        testExchangeApproval("EUR", "KMF");
    }

    @Test
    void shouldApproveUsdToKmfExchange() {
        testExchangeApproval("USD", "KMF");
    }

    @Test
    void shouldApproveKmfToEurExchange() {
        testExchangeApproval("KMF", "EUR");
    }

    @Test
    void shouldApproveKmfToUsdExchange() {
        testExchangeApproval("KMF", "USD");
    }

    @Test
    void shouldRejectExchange() {
        //given
        exchangeOperationService.init(getExchangeDto("USD", "KMF"));
        ExchangeDto initiatedLoan = exchangeOperationService.getExchangeService().findAll().get(0);

        //when
        exchangeOperationService.reject(initiatedLoan, "Just a reject reason");

        //then
        assertThat(initiatedLoan.getStatus()).isEqualTo("4"); //Rejected status = 4
        assertThat(initiatedLoan.getFailureReason()).isEqualTo("Just a reject reason");
    }


    private void testExchangeApproval(String currency1, String currency2) {
        //given
        ExchangeDto exchangeDto = getExchangeDto(currency1, currency2);
        createVaults(exchangeDto);

        exchangeOperationService.init(exchangeDto);
        ExchangeDto initiatedExchange = exchangeOperationService.getExchangeService().findAll().get(0);

        //when
        exchangeOperationService.approve(initiatedExchange);

        //then
        assertThat(initiatedExchange.getStatus()).isEqualTo("3"); //approved status = 3
    }

}