package com.admo.orderservice.service;

import com.admo.orderservice.entity.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    Customer create(Customer customer);
    List<Customer> getAll();
    Optional<Customer> getById(String customerName);
    Customer update(String customerName, Customer customer);
    void delete(String customerName);
}
