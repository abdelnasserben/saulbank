package com.dabel.service.card;

import com.dabel.dto.CardRequestDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.CardMapper;
import com.dabel.model.CardRequest;
import com.dabel.repository.CardRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class
CardRequestService {

    private final CardRequestRepository cardRequestRepository;

    public CardRequestService(CardRequestRepository cardRequestRepository) {
        this.cardRequestRepository = cardRequestRepository;
    }

    public CardRequestDto save(CardRequestDto cardRequestDto) {
        return CardMapper.toDto(cardRequestRepository.save(CardMapper.toEntity(cardRequestDto)));
    }

    public CardRequestDto findById(Long requestId) {
        CardRequest cardRequest = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Card request not found"));
        return CardMapper.toDto(cardRequest);
    }

    public List<CardRequestDto> findAll() {
        return cardRequestRepository.findAll().stream()
                .map(CardMapper::toDto)
                .toList();
    }
}
