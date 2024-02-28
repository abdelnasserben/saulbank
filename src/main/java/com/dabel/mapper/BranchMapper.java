package com.dabel.mapper;

import com.dabel.dto.BranchDto;
import com.dabel.model.Branch;
import org.modelmapper.ModelMapper;

public class BranchMapper {
    private static final ModelMapper mapper = new ModelMapper();

    public static Branch toModel(BranchDto branchDto) {
        return mapper.map(branchDto, Branch.class);
    }

    public static BranchDto toDto(Branch branch) {
        return mapper.map(branch, BranchDto.class);
    }
}
