package com.dabel.mapper;

import com.dabel.dto.TrunkDto;
import com.dabel.model.Trunk;
import org.modelmapper.ModelMapper;

public class TrunkMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Trunk toModel(TrunkDto trunkDto) {
        return mapper.map(trunkDto, Trunk.class);
    }

    public static TrunkDto toDto(Trunk trunk) {
        return mapper.map(trunk, TrunkDto.class);
    }

}
