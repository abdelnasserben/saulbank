package com.dabel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public class BranchDto {

    private Long branchId;
    private String branchName;
    private String branchAddress;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
