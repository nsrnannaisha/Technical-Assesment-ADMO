package com.admo.orderservice.mapper;

import com.admo.orderservice.dto.CustomerDto;
import com.admo.orderservice.entity.Customer;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static Customer toEntity(CustomerDto request) {
        Customer customer = new Customer();
        customer.setCustomerName(request.getCustomerName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNum(request.getPhoneNum());
        return customer;
    }

    public static CustomerDto toResponse(Customer customer) {
        return new CustomerDto(customer.getCustomerName(), customer.getEmail(), customer.getPhoneNum());
    }
}
