package com.admo.orderservice.service;

import com.admo.orderservice.entity.Customer;
import com.admo.orderservice.exception.OrderBusinessException;
import com.admo.orderservice.repository.CustomerRepository;
import com.admo.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Customer create(Customer customer) {
        ensureUnique(customer.getCustomerName(), null);
        return customerRepository.save(customer);
    }

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @Override
    public Optional<Customer> getById(String customerName) {
        return customerRepository.findById(customerName);
    }

    @Override
    @Transactional
    public Customer update(String customerName, Customer customer) {
        Customer existing = customerRepository.findById(customerName)
                .orElseThrow(() -> new OrderBusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));

        if (!customerName.equals(customer.getCustomerName())) {
            ensureUnique(customer.getCustomerName(), customerName);

            Customer renamed = new Customer();
            renamed.setCustomerName(customer.getCustomerName());
            renamed.setEmail(customer.getEmail());
            renamed.setPhoneNum(customer.getPhoneNum());
            customerRepository.save(renamed);

            List<com.admo.orderservice.entity.Order> affectedOrders = orderRepository.findAll().stream()
                    .filter(order -> order.getCustomer() != null && customerName.equals(order.getCustomerName()))
                    .peek(order -> order.assignCustomer(renamed))
                    .toList();
            orderRepository.saveAll(affectedOrders);
            customerRepository.delete(existing);
            return renamed;
        }

        existing.setEmail(customer.getEmail());
        existing.setPhoneNum(customer.getPhoneNum());
        return customerRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(String customerName) {
        if (!customerRepository.existsById(customerName)) {
            throw new OrderBusinessException("CUSTOMER_NOT_FOUND", "Customer not found");
        }

        boolean hasOrders = orderRepository.findAll().stream()
                .anyMatch(order -> order.getCustomer() != null && customerName.equals(order.getCustomerName()));
        if (hasOrders) {
            throw new OrderBusinessException("CUSTOMER_HAS_ORDERS", "Customer cannot be deleted while orders still exist");
        }

        customerRepository.deleteById(customerName);
    }

    private void ensureUnique(String customerName, String currentName) {
        boolean exists = customerRepository.existsById(customerName) && (currentName == null || !currentName.equals(customerName));
        if (exists) {
            throw new OrderBusinessException("CUSTOMER_NAME_ALREADY_USED", "Customer name already used");
        }
    }
}
