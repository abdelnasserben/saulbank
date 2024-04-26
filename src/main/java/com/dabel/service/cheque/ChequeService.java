package com.dabel.service.cheque;

import com.dabel.dto.ChequeDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.ChequeMapper;
import com.dabel.repository.ChequeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChequeService {

    private final ChequeRepository chequeRepository;

    @Autowired
    public ChequeService(ChequeRepository chequeRepository) {
        this.chequeRepository = chequeRepository;
    }

    public ChequeDto save(ChequeDto chequeDto) {
        return ChequeMapper.toDTO(chequeRepository.save(ChequeMapper.toEntity(chequeDto)));
    }

    public ChequeDto findById(Long cardId) {
        return ChequeMapper.toDTO(chequeRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Cheque not found")));
    }

    public ChequeDto findByChequeNumber(String chequeNumber) {
        return ChequeMapper.toDTO(chequeRepository.findByChequeNumber(chequeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cheque not found")));
    }

    public List<ChequeDto> findAll() {
        return chequeRepository.findAll().stream()
                .map(ChequeMapper::toDTO)
                .toList();
    }
}
