package com.dabel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public class LoginLogDto implements StatedObject {

    private Long id;
    private UserDto user;
    private String ipAddress;
    private String os;
    private LocalDateTime loginAt;
    private LocalDateTime logoutAt;
    private String status;
}

