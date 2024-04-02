package com.dabel.controller;

import com.dabel.app.CurrencyExchanger;
import com.dabel.constant.BankFees;
import com.dabel.constant.Currency;
import com.dabel.dto.AccountDto;
import com.dabel.service.account.AccountFacadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppRestController {

    private final AccountFacadeService accountFacadeService;

    public AppRestController(AccountFacadeService accountFacadeService) {
        this.accountFacadeService = accountFacadeService;
    }

    @GetMapping("/rest/account/" + "{accountNumber}")
    public ResponseEntity<AccountDto> getAccountInformation(@PathVariable String accountNumber) {

        return ResponseEntity.ok(accountFacadeService.findByNumber(accountNumber));
    }

    @GetMapping("/rest/baseCurrencyInfo/" + "{currency1}-{currency2}-{amount}")
    public ResponseEntity<double[]> getCurrencyConversionBase(@PathVariable Currency currency1, @PathVariable Currency currency2, @PathVariable double amount) {

        if(currency1.equals(currency2))
            return ResponseEntity.badRequest().build();

        double conversionRate =  switch (CurrencyExchanger.getExchangeType(currency1.name(), currency2.name())) {
            case EUR_KMF -> BankFees.Exchange.BUY_EUR;
            case KMF_EUR -> BankFees.Exchange.SALE_EUR;
            case USD_KMF -> BankFees.Exchange.BUY_USD;
            case KMF_USD -> BankFees.Exchange.SALE_USD;
        };

        double conversionAmount = CurrencyExchanger.exchange(currency1.name(), currency2.name(), amount);

        return ResponseEntity.ok(new double[]{conversionRate, conversionAmount});
    }
}
