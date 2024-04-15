package com.dabel.service.cheque;

import com.dabel.dto.ChequeRequestDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.ChequeMapper;
import com.dabel.model.ChequeRequest;
import com.dabel.repository.ChequeRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class
ChequeRequestService {

    private final ChequeRequestRepository chequeRequestRepository;

    public ChequeRequestService(ChequeRequestRepository chequeRequestRepository) {
        this.chequeRequestRepository = chequeRequestRepository;
    }

    public ChequeRequestDto save(ChequeRequestDto chequeRequestDto) {
        return ChequeMapper.toDTO(chequeRequestRepository.save(ChequeMapper.toEntity(chequeRequestDto)));
    }

    public ChequeRequestDto findById(Long requestId) {
        ChequeRequest chequeRequest = chequeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Cheque request not found"));
        return ChequeMapper.toDTO(chequeRequest);
    }

    public List<ChequeRequestDto> findAll() {
        return chequeRequestRepository.findAll().stream()
                .map(ChequeMapper::toDTO)
                .toList();
    }
}
