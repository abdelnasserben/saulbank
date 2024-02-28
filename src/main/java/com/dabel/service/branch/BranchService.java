package com.dabel.service.branch;

import com.dabel.dto.BranchDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.BranchMapper;
import com.dabel.model.Branch;
import com.dabel.repository.BranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {

    BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public BranchDto save(BranchDto branchDTO) {
        Branch branch = branchRepository.save(BranchMapper.toModel(branchDTO));
        return BranchMapper.toDto(branch);
    }

    public BranchDto findById(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        return BranchMapper.toDto(branch);
    }

    public List<BranchDto> findAll() {
        return branchRepository.findAll().stream()
                .map(BranchMapper::toDto)
                .collect(Collectors.toList());
    }
}
