package com.dabel.mapper;

import com.dabel.dto.ChequeDto;
import com.dabel.dto.ChequeRequestDto;
import com.dabel.model.Cheque;
import com.dabel.model.ChequeRequest;
import org.modelmapper.ModelMapper;

public class ChequeMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Cheque toEntity(ChequeDto chequeDto) {
        return mapper.map(chequeDto, Cheque.class);
    }

    public static ChequeRequest toEntity(ChequeRequestDto chequeRequestDto) {
        return mapper.map(chequeRequestDto, ChequeRequest.class);
    }

    public static ChequeDto toDTO(Cheque cheque) {
        return mapper.map(cheque, ChequeDto.class);
    }

    public static ChequeRequestDto toDTO(ChequeRequest chequeRequest) {
        return mapper.map(chequeRequest, ChequeRequestDto.class);
    }
}
