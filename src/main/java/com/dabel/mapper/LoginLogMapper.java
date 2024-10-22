package com.dabel.mapper;

import com.dabel.dto.LoginLogDto;
import com.dabel.model.LoginLog;
import org.modelmapper.ModelMapper;

public class LoginLogMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static LoginLog toEntity(LoginLogDto loginLogDto) {
        return mapper.map(loginLogDto, LoginLog.class);
    }

    public static LoginLogDto toDTO(LoginLog loginLog) {
        return mapper.map(loginLog, LoginLogDto.class);
    }

}
