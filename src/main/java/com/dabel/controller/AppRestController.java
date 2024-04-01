package com.dabel.controller;

import com.dabel.app.CurrencyExchanger;
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

    @GetMapping("/rest/baseCurrencyConversion/" + "{currency}-{amount}")
    public ResponseEntity<double[]> getBaseCurrencyConversion(@PathVariable Currency currency, @PathVariable double amount) {

        return ResponseEntity.ok(CurrencyExchanger.getBaseConversion(currency.name(), Currency.KMF.name(), amount));
    }
}
