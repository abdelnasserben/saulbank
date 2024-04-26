package com.dabel.service.exchange;

import com.dabel.dto.ExchangeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExchangeFacadeService {

    private final ExchangeOperationService exchangeOperationService;

    @Autowired
    public ExchangeFacadeService(ExchangeOperationService exchangeOperationService) {
        this.exchangeOperationService = exchangeOperationService;
    }

    public void init(ExchangeDto exchangeDto) {
        exchangeOperationService.init(exchangeDto);
    }

    public void approve(Long operationId) {
        exchangeOperationService.approve(this.findById(operationId));
    }

    public void reject(Long operationId, String remarks) {
        exchangeOperationService.reject(this.findById(operationId), remarks);
    }

    public List<ExchangeDto> findAll() {
        return this.exchangeOperationService.getExchangeService().findAll();
    }

    public ExchangeDto findById(Long exchangeId) {
        return this.exchangeOperationService.getExchangeService().findById(exchangeId);
    }

    public List<ExchangeDto> findAllByCustomerIdentity(String customerIdentity) {
        return this.exchangeOperationService.getExchangeService().findAllByCustomerIdentity(customerIdentity);
    }

}
