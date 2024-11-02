package com.dabel.service.loan;

import com.dabel.dto.CustomerDto;
import com.dabel.dto.LoanDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.CustomerMapper;
import com.dabel.mapper.LoanMapper;
import com.dabel.repository.LoanRepository;
import com.dabel.service.customer.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final CustomerService customerService;

    public LoanService(LoanRepository loanRepository, CustomerService customerService) {
        this.loanRepository = loanRepository;
        this.customerService = customerService;
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
        CustomerDto customerDto = customerService.findByIdentity(customerIdentityNumber);
        return loanRepository.findAllByBorrower(CustomerMapper.toEntity(customerDto)).stream()
                .map(LoanMapper::toDTO)
                .collect(Collectors.toList());
    }
}
