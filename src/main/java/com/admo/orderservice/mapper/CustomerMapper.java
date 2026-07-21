package com.admo.orderservice.mapper;

import com.admo.orderservice.dto.CustomerCreateRequest;
import com.admo.orderservice.dto.CustomerDetailResponse;
import com.admo.orderservice.dto.CustomerUpdateRequest;
import com.admo.orderservice.entity.Customer;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static Customer toEntity(CustomerCreateRequest request) {
        Customer customer = new Customer();
        customer.setCustomerName(request.getCustomerName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNum(request.getPhoneNum());
        return customer;
    }

    public static Customer toEntity(CustomerUpdateRequest request) {
        Customer customer = new Customer();
        customer.setCustomerName(request.getCustomerName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNum(request.getPhoneNum());
        return customer;
    }

    public static CustomerDetailResponse toResponse(Customer customer) {
        return new CustomerDetailResponse(customer.getCustomerName(), customer.getEmail(), customer.getPhoneNum());
    }
}
