package com.dabel.controller;

import com.dabel.app.CurrencyExchanger;
import com.dabel.app.Helper;
import com.dabel.constant.BankFees;
import com.dabel.constant.Currency;
import com.dabel.dto.AccountDto;
import com.dabel.dto.ChequeDto;
import com.dabel.dto.CustomerDto;
import com.dabel.service.account.AccountFacadeService;
import com.dabel.service.cheque.ChequeFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppRestController {

    private static final String CUSTOMER_PATH = "/rest/customer";
    private static final String ACCOUNT_PATH = "/rest/account";
    private static final String CURRENCY_CONVERSION_PATH = "/rest/baseCurrencyInfo";
    private static final String LOAN_TOTAL_DUE_PATH = "/rest/loanTotalDue";
    private static final String CHEQUE_PATH = "/rest/cheque";

    private final AccountFacadeService accountFacadeService;
    private final CustomerFacadeService customerFacadeService;
    private final ChequeFacadeService chequeFacadeService;

    public AppRestController(AccountFacadeService accountFacadeService, CustomerFacadeService customerFacadeService, ChequeFacadeService chequeFacadeService) {
        this.accountFacadeService = accountFacadeService;
        this.customerFacadeService = customerFacadeService;
        this.chequeFacadeService = chequeFacadeService;
    }

    @GetMapping(CUSTOMER_PATH + "/{identityNumber}")
    public ResponseEntity<CustomerDto> getCustomerInformation(@PathVariable String identityNumber) {

        return ResponseEntity.ok(customerFacadeService.getByIdentityNumber(identityNumber));
    }

    @GetMapping(CUSTOMER_PATH + "/accounts/{identityNumber}")
    public ResponseEntity<Object[]> getCustomerAccounts(@PathVariable String identityNumber) {

        CustomerDto customer = customerFacadeService.getByIdentityNumber(identityNumber);
        Object[] accountNumbers = accountFacadeService.getAllTrunksByCustomer(customer).stream()
                .map(trunkDto -> trunkDto.getAccount().getAccountNumber())
                .toArray();
        return ResponseEntity.ok(accountNumbers);
    }

    @GetMapping(ACCOUNT_PATH + "/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountInformation(@PathVariable String accountNumber) {

        return ResponseEntity.ok(accountFacadeService.getAccountByNumber(accountNumber));
    }

    @GetMapping(CURRENCY_CONVERSION_PATH + "/{currency1}-{currency2}-{amount}")
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

    @GetMapping(LOAN_TOTAL_DUE_PATH + "/{amount}-{interestRate}")
    public ResponseEntity<Double> getLoanTotalDueAmount(@PathVariable double amount, @PathVariable double interestRate) {

        return ResponseEntity.ok(Helper.calculateTotalAmountOfLoan(amount, interestRate));
    }

    @GetMapping(CHEQUE_PATH + "/{chequeNumber}")
    ResponseEntity<ChequeDto> getChequeInformation(@PathVariable String chequeNumber) {
        return ResponseEntity.ok(chequeFacadeService.findChequeByNumber(chequeNumber));
    }
}
