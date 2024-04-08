package com.dabel.service.loan;

import com.dabel.dto.CustomerDto;
import com.dabel.dto.LoanDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.CustomerMapper;
import com.dabel.mapper.LoanMapper;
import com.dabel.repository.LoanRepository;
import com.dabel.service.customer.CustomerFacadeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final CustomerFacadeService customerFacadeService;

    public LoanService(LoanRepository loanRepository, CustomerFacadeService customerFacadeService) {
        this.loanRepository = loanRepository;
        this.customerFacadeService = customerFacadeService;
    }

    public LoanDto save(LoanDto loanDTO) {
        return LoanMapper.toDTO(loanRepository.save(LoanMapper.toEntity(loanDTO)));
    }

    public LoanDto findById(Long loanId) {
        return LoanMapper.toDTO(loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found")));
    }

    public List<LoanDto> findAll() {
        return loanRepository.findAll().stream()
                .map(LoanMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<LoanDto> findAllByCustomerIdentity(String customerIdentityNumber) {
        CustomerDto customerDto = customerFacadeService.findByIdentity(customerIdentityNumber);
        return loanRepository.findAllByBorrower(CustomerMapper.toModel(customerDto)).stream()
                .map(LoanMapper::toDTO)
                .collect(Collectors.toList());
    }
}
