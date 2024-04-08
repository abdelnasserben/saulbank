package com.dabel.service.transaction;

import com.dabel.dto.AccountDto;
import com.dabel.dto.TransactionDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionFacadeService {

    private final TransactionService transactionService;
    private final TransactionContext transactionContext;

    public TransactionFacadeService(TransactionService transactionService, TransactionContext transactionContext) {
        this.transactionService = transactionService;
        this.transactionContext = transactionContext;
    }

    public void save(TransactionDto transactionDto) {
        transactionService.save(transactionDto);
    }

    public void init(TransactionDto transactionDto) {
        transactionContext.setContext(transactionDto.getTransactionType()).init(transactionDto);
    }

    public void approve(Long transactionId) {
        TransactionDto transactionDto = transactionService.findById(transactionId);
        transactionContext.setContext(transactionDto.getTransactionType()).approve(transactionDto);
    }

    public void reject(Long transactionId, String remarks) {
        TransactionDto transactionDto = transactionService.findById(transactionId);
        transactionContext.setContext(transactionDto.getTransactionType()).reject(transactionDto, remarks);
    }

    public List<TransactionDto> findAll() {
        return transactionService.findAll();
    }

    public TransactionDto findById(Long transactionId) {
        return transactionService.findById(transactionId);
    }

    public List<TransactionDto> findAllByAccount(AccountDto accountDto) {
        return transactionService.findAllByAccount(accountDto);
    }
}
