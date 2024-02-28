package com.dabel.app;

import com.dabel.dto.BranchDto;

public record Fee(BranchDto branchDto, double value, String description) {
}
