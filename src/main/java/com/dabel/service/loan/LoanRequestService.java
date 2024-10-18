package com.dabel.service.loan;

import com.dabel.dto.LoanRequestDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.LoanMapper;
import com.dabel.model.LoanRequest;
import com.dabel.repository.LoanRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class
LoanRequestService {

    private final LoanRequestRepository loanRequestRepository;

    @Autowired
    public LoanRequestService(LoanRequestRepository loanRequestRepository) {
        this.loanRequestRepository = loanRequestRepository;
    }

    public LoanRequestDto save(LoanRequestDto loanRequestDto) {
        return LoanMapper.toDto(loanRequestRepository.save(LoanMapper.toEntity(loanRequestDto)));
    }

    public LoanRequestDto findById(Long requestId) {
        LoanRequest cardRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Card request not found"));
        return LoanMapper.toDto(cardRequest);
    }

    public List<LoanRequestDto> findAll() {
        return loanRequestRepository.findAll().stream()
                .map(LoanMapper::toDto)
                .toList();
    }
}
