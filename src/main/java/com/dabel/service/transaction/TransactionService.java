package com.dabel.service.transaction;

import com.dabel.dto.AccountDto;
import com.dabel.dto.TransactionDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.AccountMapper;
import com.dabel.mapper.TransactionMapper;
import com.dabel.model.Account;
import com.dabel.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionDto save(TransactionDto transactionDto) {
        return TransactionMapper.toDto(transactionRepository.save(TransactionMapper.toModel(transactionDto)));
    }

    public List<TransactionDto> findAll() {

        return transactionRepository.findAll().stream()
                .map(TransactionMapper::toDto)
                .toList();
    }

    public TransactionDto findById(Long transactionId) {
        return TransactionMapper.toDto(transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found")));
    }

    public List<TransactionDto> findAllByAccount(AccountDto accountDto) {
        Account account = AccountMapper.toModel(accountDto);
        return transactionRepository.findAllByInitiatorAccountOrReceiverAccount(account, account).stream()
                .map(TransactionMapper::toDto)
                .toList();
    }
}
