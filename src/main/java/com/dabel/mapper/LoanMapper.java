package com.dabel.mapper;

import com.dabel.dto.LoanDto;
import com.dabel.dto.LoanRequestDto;
import com.dabel.model.Loan;
import com.dabel.model.LoanRequest;
import org.modelmapper.ModelMapper;

public class LoanMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Loan toEntity(LoanDto loanDto) {
        return mapper.map(loanDto, Loan.class);
    }

    public static LoanRequest toEntity(LoanRequestDto loanRequestDto) {
        return mapper.map(loanRequestDto, LoanRequest.class);
    }

    public static LoanDto toDTO(Loan loan) {
        return mapper.map(loan, LoanDto.class);
    }

    public static LoanRequestDto toDto(LoanRequest loanRequest) {
        return mapper.map(loanRequest, LoanRequestDto.class);
    }
}
