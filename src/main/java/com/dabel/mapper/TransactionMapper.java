package com.dabel.mapper;

import com.dabel.dto.TransactionDto;
import com.dabel.model.Transaction;
import org.modelmapper.ModelMapper;

public class TransactionMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Transaction toModel(TransactionDto transactionDto) {
        return mapper.map(transactionDto, Transaction.class);
    }

    public static TransactionDto toDto(Transaction transaction) {
        return mapper.map(transaction, TransactionDto.class);
    }

}
