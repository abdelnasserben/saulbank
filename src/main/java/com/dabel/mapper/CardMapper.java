package com.dabel.mapper;

import com.dabel.dto.CardDto;
import com.dabel.dto.CardRequestDto;
import com.dabel.model.Card;
import com.dabel.model.CardRequest;
import org.modelmapper.ModelMapper;

public class CardMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Card toEntity(CardDto cardDto) {
        return mapper.map(cardDto, Card.class);
    }

    public static CardDto toDTO(Card card) {
        return mapper.map(card, CardDto.class);
    }

    public static CardRequest toEntity(CardRequestDto cardRequestDto) {
        return mapper.map(cardRequestDto, CardRequest.class);
    }

    public static CardRequestDto toDTO(CardRequest cardRequest) {
        return mapper.map(cardRequest, CardRequestDto.class);
    }
}
