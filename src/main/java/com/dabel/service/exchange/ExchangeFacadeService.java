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
        exchangeOperationService.approve(this.getById(operationId));
    }

    public void reject(Long operationId, String remarks) {
        exchangeOperationService.reject(this.getById(operationId), remarks);
    }

    public List<ExchangeDto> getAll() {
        return this.exchangeOperationService.getExchangeService().findAll();
    }

    public ExchangeDto getById(Long exchangeId) {
        return this.exchangeOperationService.getExchangeService().findById(exchangeId);
    }

    public List<ExchangeDto> getAllByCustomerIdentityNumber(String customerIdentity) {
        return this.exchangeOperationService.getExchangeService().findAllByCustomerIdentity(customerIdentity);
    }

}
