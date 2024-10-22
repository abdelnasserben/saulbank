package com.dabel.mapper;

import com.dabel.dto.UserLogDto;
import com.dabel.model.UserLog;
import org.modelmapper.ModelMapper;

public class UserLogMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static UserLog toEntity(UserLogDto userLogDto) {
        return mapper.map(userLogDto, UserLog.class);
    }

    public static UserLogDto toDTO(UserLog userLog) {
        return mapper.map(userLog, UserLogDto.class);
    }

}
