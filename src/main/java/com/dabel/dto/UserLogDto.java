package com.dabel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public class UserLogDto {

    private Long logId;
    private UserDto user;
    private String httpMethod;
    private String httpStatus;
    private String url;
    private LocalDateTime timestamp;
}
