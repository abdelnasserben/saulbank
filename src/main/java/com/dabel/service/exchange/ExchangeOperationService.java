package com.dabel.service.exchange;

import com.dabel.app.CurrencyExchanger;
import com.dabel.constant.Currency;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.ExchangeDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.EvaluableOperation;
import com.dabel.service.account.AccountService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExchangeOperationService implements EvaluableOperation<ExchangeDto> {

    @Getter
    private final ExchangeService exchangeService;
    private final AccountService accountService;

    @Autowired
    public ExchangeOperationService(ExchangeService exchangeService, AccountService accountService) {
        this.exchangeService = exchangeService;
        this.accountService = accountService;
    }

    @Override
    public void init(ExchangeDto exchangeDto) {
        String purchaseCurrency = exchangeDto.getPurchaseCurrency();
        String saleCurrency = exchangeDto.getSaleCurrency();

        if(!purchaseCurrency.equalsIgnoreCase(Currency.KMF.name()) && !saleCurrency.equalsIgnoreCase(Currency.KMF.name()))
            throw new IllegalOperationException("An exchange must involve KMF currency");

        if(purchaseCurrency.equalsIgnoreCase(saleCurrency))
            throw new IllegalOperationException("Currencies must be different");

        //TODO: convert amount and set status of exchange before saving
        double saleAmount = CurrencyExchanger.exchange(purchaseCurrency, saleCurrency, exchangeDto.getPurchaseAmount());
        exchangeDto.setSaleAmount(saleAmount);
        exchangeDto.setStatus(Status.PENDING.code());
        exchangeService.save(exchangeDto);
    }

    @Override
    public void approve(ExchangeDto exchangeDto) {

        //TODO: define purchase account and sale account
        AccountDto purchaseAccount, saleAccount;

        saleAccount = switch (CurrencyExchanger.getExchangeType(exchangeDto.getPurchaseCurrency(), exchangeDto.getSaleCurrency())) {
            case KMF_EUR -> {
                purchaseAccount = accountService.findVault(exchangeDto.getBranch(), Currency.KMF.name());
                yield accountService.findVault(exchangeDto.getBranch(), Currency.EUR.name());
            }
            case EUR_KMF -> {
                purchaseAccount = accountService.findVault(exchangeDto.getBranch(), Currency.EUR.name());
                yield accountService.findVault(exchangeDto.getBranch(), Currency.KMF.name());
            }
            case KMF_USD -> {
                purchaseAccount = accountService.findVault(exchangeDto.getBranch(), Currency.KMF.name());
                yield accountService.findVault(exchangeDto.getBranch(), Currency.USD.name());
            }
            case USD_KMF -> {
                purchaseAccount = accountService.findVault(exchangeDto.getBranch(), Currency.USD.name());
                yield accountService.findVault(exchangeDto.getBranch(), Currency.KMF.name());
            }
        };

        accountService.debit(saleAccount, exchangeDto.getSaleAmount());
        accountService.credit(purchaseAccount, exchangeDto.getPurchaseAmount());

        exchangeDto.setStatus(Status.APPROVED.code());
//        we'll make updated by later

        exchangeService.save(exchangeDto);
    }

    @Override
    public void reject(ExchangeDto exchangeDto, String remarks) {
        exchangeDto.setStatus(Status.REJECTED.code());
        exchangeDto.setFailureReason(remarks);
//        we'll make updated by later

        exchangeService.save(exchangeDto);
    }
}
