package com.dabel.mapper;

import com.dabel.dto.UserDto;
import com.dabel.model.User;
import org.modelmapper.ModelMapper;

public class UserMapper {
    private static final ModelMapper mapper = new ModelMapper();

    public static User toEntity(UserDto userDto) {
        return mapper.map(userDto, User.class);
    }

    public static UserDto toDto(User user) {
        return mapper.map(user, UserDto.class);
    }
}
