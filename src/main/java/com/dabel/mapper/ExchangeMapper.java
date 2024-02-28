package com.dabel.mapper;

import com.dabel.dto.ExchangeDto;
import com.dabel.model.Exchange;
import org.modelmapper.ModelMapper;

public class ExchangeMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Exchange toEntity(ExchangeDto exchangeDto) {
        return mapper.map(exchangeDto, Exchange.class);
    }

    public static ExchangeDto toDTO(Exchange exchange) {
        return mapper.map(exchange, ExchangeDto.class);
    }

}
