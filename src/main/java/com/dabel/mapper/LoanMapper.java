package com.dabel.mapper;

import com.dabel.dto.LoanDto;
import com.dabel.model.Loan;
import org.modelmapper.ModelMapper;

public class LoanMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Loan toEntity(LoanDto loanDto) {
        return mapper.map(loanDto, Loan.class);
    }

    public static LoanDto toDTO(Loan loan) {
        return mapper.map(loan, LoanDto.class);
    }
}
