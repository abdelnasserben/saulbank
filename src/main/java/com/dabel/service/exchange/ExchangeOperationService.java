package com.dabel.service.exchange;

import com.dabel.app.CurrencyExchanger;
import com.dabel.constant.Status;
import com.dabel.dto.ExchangeDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.EvaluableOperation;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class ExchangeOperationService implements EvaluableOperation<ExchangeDto> {

    private final ExchangeService exchangeService;

    public ExchangeOperationService(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @Override
    public void init(ExchangeDto exchangeDto) {
        String purchaseCurrency = exchangeDto.getPurchaseCurrency();
        String saleCurrency = exchangeDto.getSaleCurrency();

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
        exchangeDto.setStatus(Status.APPROVED.code());
//        exchange.setUpdatedBy("Administrator");
//        exchange.setUpdatedAt(LocalDateTime.now());
//        we'll make updated by later

        exchangeService.save(exchangeDto);
    }

    @Override
    public void reject(ExchangeDto exchangeDto, String remarks) {
        exchangeDto.setStatus(Status.REJECTED.code());
        exchangeDto.setFailureReason(remarks);
//        exchange.setUpdatedBy("Administrator");
//        exchange.setUpdatedAt(LocalDateTime.now());
//        we'll make updated by later

        exchangeService.save(exchangeDto);
    }
}
