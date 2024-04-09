package com.dabel.mapper;

import com.dabel.dto.ChequeDto;
import com.dabel.model.Cheque;
import org.modelmapper.ModelMapper;

public class ChequeMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Cheque toEntity(ChequeDto chequeDto) {
        return mapper.map(chequeDto, Cheque.class);
    }

    public static ChequeDto toDTO(Cheque cheque) {
        return mapper.map(cheque, ChequeDto.class);
    }
}
