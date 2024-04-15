package com.dabel.service.cheque;

import com.dabel.dto.ChequeDto;
import com.dabel.dto.TrunkDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.ChequeMapper;
import com.dabel.mapper.TrunkMapper;
import com.dabel.repository.ChequeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChequeService {

    private final ChequeRepository chequeRepository;

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

    public List<ChequeDto> findAllByTrunk(TrunkDto trunkDto) {

        return chequeRepository.findAllByTrunk(TrunkMapper.toModel(trunkDto)).stream()
                .map(ChequeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ChequeDto> findAll() {
        return chequeRepository.findAll().stream()
                .map(ChequeMapper::toDTO)
                .toList();
    }
}
