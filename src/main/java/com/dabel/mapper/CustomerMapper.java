package com.dabel.mapper;

import com.dabel.dto.CustomerDto;
import com.dabel.model.Customer;
import org.modelmapper.ModelMapper;

public class CustomerMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Customer toModel(CustomerDto customerDto) {
        return mapper.map(customerDto, Customer.class);
    }

    public static CustomerDto toDto(Customer customer) {
        return mapper.map(customer, CustomerDto.class);
    }

}
