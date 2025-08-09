package com.dabel.service.exchange;

import com.dabel.app.CurrencyExchanger;
import com.dabel.app.Helper;
import com.dabel.constant.Currency;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.ExchangeDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.account.AccountService;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class ExchangeOperationService implements EvaluableOperation<ExchangeDto> {

    @Getter
    private final ExchangeService exchangeService;
    private final AccountService accountService;

    public ExchangeOperationService(ExchangeService exchangeService, AccountService accountService) {
        this.exchangeService = exchangeService;
        this.accountService = accountService;
    }

    @Override
    public void init(ExchangeDto exchangeDto) {

        //TODO: validate exchange currencies
        validateCurrencies(exchangeDto.getPurchaseCurrency(), exchangeDto.getSaleCurrency());

        //TODO: set up exchange sale amount
        double saleAmount = CurrencyExchanger.exchange(exchangeDto.getPurchaseCurrency(), exchangeDto.getSaleCurrency(), exchangeDto.getPurchaseAmount());
        exchangeDto.setSaleAmount(saleAmount);

        applyExchangeUpdates(exchangeDto, Status.PENDING.code(), null);
    }

    @Override
    public void approve(ExchangeDto exchangeDto) {

        //TODO: define purchase account and sale account
        AccountDto purchaseAccount, saleAccount;
        BranchDto branch = exchangeDto.getBranch();

        saleAccount = switch (CurrencyExchanger.getExchangeType(exchangeDto.getPurchaseCurrency(), exchangeDto.getSaleCurrency())) {
            case KMF_EUR -> {
                purchaseAccount = retrieveVaultByBranchAndCurrency(branch, Currency.KMF.name());
                yield retrieveVaultByBranchAndCurrency(branch, Currency.EUR.name());
            }
            case EUR_KMF -> {
                purchaseAccount = retrieveVaultByBranchAndCurrency(branch, Currency.EUR.name());
                yield retrieveVaultByBranchAndCurrency(branch, Currency.KMF.name());
            }
            case KMF_USD -> {
                purchaseAccount = retrieveVaultByBranchAndCurrency(branch, Currency.KMF.name());
                yield retrieveVaultByBranchAndCurrency(branch, Currency.USD.name());
            }
            case USD_KMF -> {
                purchaseAccount = retrieveVaultByBranchAndCurrency(branch, Currency.USD.name());
                yield retrieveVaultByBranchAndCurrency(branch, Currency.KMF.name());
            }
            default -> throw new IllegalOperationException("Unsupported currency exchange operation");
        };

        accountService.debitAccount(saleAccount, exchangeDto.getSaleAmount());
        accountService.creditAccount(purchaseAccount, exchangeDto.getPurchaseAmount());

        applyExchangeUpdates(exchangeDto, Status.APPROVED.code(), "Approved");
    }

    @Override
    public void reject(ExchangeDto exchangeDto, String remarks) {
        applyExchangeUpdates(exchangeDto, Status.REJECTED.code(), remarks);
    }

    private void validateCurrencies(String purchaseCurrency, String saleCurrency) {
        if (!purchaseCurrency.equalsIgnoreCase(Currency.KMF.name()) &&
                !saleCurrency.equalsIgnoreCase(Currency.KMF.name())) {
            throw new IllegalOperationException("An exchange must involve KMF currency");
        }

        if (purchaseCurrency.equalsIgnoreCase(saleCurrency)) {
            throw new IllegalOperationException("Currencies must be different");
        }
    }

    private AccountDto retrieveVaultByBranchAndCurrency(BranchDto branchDto, String currency) {
        return accountService.findVaultByBranchAndCurrency(branchDto, currency);
    }

    private void applyExchangeUpdates(ExchangeDto exchangeDto, String status, String failureReason) {
        exchangeDto.setStatus(status);
        exchangeDto.setFailureReason(failureReason);

        String operator = Helper.getAuthenticated().getName();
        if(exchangeDto.getExchangeId() != null)
            exchangeDto.setUpdatedBy(operator);
        else
            exchangeDto.setInitiatedBy(operator);

        exchangeService.save(exchangeDto);
    }
}
