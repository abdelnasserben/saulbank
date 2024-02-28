package com.dabel.service.customer;

import com.dabel.dto.CustomerDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.CustomerMapper;
import com.dabel.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerDto save(CustomerDto customerDTO) {
        return CustomerMapper.toDto(customerRepository.save(CustomerMapper.toModel(customerDTO)));
    }

    public List<CustomerDto> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerMapper::toDto)
                .toList();
    }

    public CustomerDto findByIdentity(String identityNumber) {
        return CustomerMapper.toDto(customerRepository.findByIdentityNumber(identityNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found")));
    }

}
