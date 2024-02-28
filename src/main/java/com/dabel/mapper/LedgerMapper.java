package com.dabel.mapper;

import com.dabel.dto.LedgerDto;
import com.dabel.model.Ledger;
import org.modelmapper.ModelMapper;

public class LedgerMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Ledger toModel(LedgerDto ledgerDto) {
        return mapper.map(ledgerDto, Ledger.class);
    }

    public static LedgerDto toDto(Ledger ledger) {
        return mapper.map(ledger, LedgerDto.class);
    }

}
