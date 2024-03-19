package com.dabel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public class BranchDto implements StatedObject {

    private Long branchId;
    @NotBlank
    private String branchName;
    @NotBlank
    private String branchAddress;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
