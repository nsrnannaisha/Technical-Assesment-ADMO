package com.admo.orderservice.service;

import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.entity.OrderStatus;
import com.admo.orderservice.entity.OrderSortKey;
import com.admo.orderservice.entity.Customer;
import com.admo.orderservice.exception.OrderNotFoundException;
import com.admo.orderservice.exception.OrderBusinessException;
import com.admo.orderservice.repository.OrderRepository;
import com.admo.orderservice.repository.CustomerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.Comparator;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final CustomerRepository customerRepository;

    public OrderServiceImpl(OrderRepository repository, CustomerRepository customerRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Order create(Order order) {
        order.assignCustomer(resolveCustomer(order.getCustomer()));
        return repository.save(order);
    }

    @Override
    public Optional<Order> getById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Order> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public Optional<Order> update(UUID id, Customer customer, List<LineItem> items) {
        return repository.findById(id).map(order -> {
            order.applyUpdate(resolveCustomer(customer), items);
            return repository.save(order);
        });
    }

    @Override
    @Transactional
    public Optional<Order> update(UUID id, String customerName, List<LineItem> items) {
        return repository.findById(id).map(order -> {
            Customer existingCustomer = customerRepository.findById(customerName).orElseThrow(() -> new OrderBusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));
            order.applyUpdate(existingCustomer, items);
            return repository.save(order);
        });
    }

    @Override
    public boolean delete(UUID id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public Order changeStatus(UUID id, OrderStatus newStatus, String reason) {
        Order order = repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        order.changeStatus(newStatus, reason);
        return repository.save(order);
    }

    @Override
    public Page<Order> getAll(Pageable pageable, String sortKey) {
        Comparator<Order> comparator = OrderSortKey.fromKey(sortKey).comparator();
        List<Order> sorted = repository.findAll().stream().sorted(comparator).toList();
        int from = Math.min((int) pageable.getOffset(), sorted.size());
        int to = Math.min(from + pageable.getPageSize(), sorted.size());

        return new PageImpl<>(sorted.subList(from, to), pageable, sorted.size());
    }

    private Customer resolveCustomer(Customer customer) {
        if (customer == null) {
            return null;
        }
        return customerRepository.findById(customer.getCustomerName())
                .orElseGet(() -> customerRepository.save(customer));
    }
}
