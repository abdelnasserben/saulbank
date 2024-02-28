package com.dabel.service.exchange;

import com.dabel.dto.ExchangeDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.ExchangeMapper;
import com.dabel.model.Exchange;
import com.dabel.repository.ExchangeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;

    public ExchangeService(ExchangeRepository exchangeRepository) {
        this.exchangeRepository = exchangeRepository;
    }

    public ExchangeDto save(ExchangeDto exchangeDto) {
        Exchange exchange = exchangeRepository.save(ExchangeMapper.toEntity(exchangeDto));
        return ExchangeMapper.toDTO(exchange);
    }

    public List<ExchangeDto> findAll() {
        return exchangeRepository.findAll().stream()
                .map(ExchangeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ExchangeDto findById(Long exchangeId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));
        return ExchangeMapper.toDTO(exchange);
    }

    public List<ExchangeDto> findAllByCustomerIdentity(String customerIdentity) {
        return exchangeRepository.findAllByCustomerIdentityNumber(customerIdentity).stream()
                .map(ExchangeMapper::toDTO)
                .collect(Collectors.toList());
    }
}
