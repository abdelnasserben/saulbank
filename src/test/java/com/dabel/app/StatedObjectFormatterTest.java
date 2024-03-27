package com.dabel.app;

import com.dabel.constant.Status;
import com.dabel.dto.CardDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatedObjectFormatterTest {

    @Test
    void shouldFormatOneStatedObject() {
        //given
        CardDto cardDto = CardDto.builder()
                .cardName("John Doe")
                .cardNumber("4111 1111 1111 1111")
                .status(Status.PENDING.code())
                .build();

        //when
        CardDto expected = StatedObjectFormatter.format(cardDto);

        //then
        assertThat(expected.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldFormatListOfStatedObjects() {
        //given
        CardDto cardDto1 = CardDto.builder()
                .cardName("John Doe")
                .cardNumber("4111 1111 1111 1111")
                .status(Status.PENDING.code())
                .build();

        CardDto cardDto2 = CardDto.builder()
                .cardName("Sarah Hunt")
                .cardNumber("5555 0088 2002 7894")
                .status(Status.ACTIVE.code())
                .build();

        List<CardDto> cardDtoList = List.of(cardDto1, cardDto2);

        //when
        List<CardDto> expected = StatedObjectFormatter.format(cardDtoList);

        //then
        assertThat(expected.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(expected.get(1).getStatus()).isEqualTo("ACTIVE");
    }
}