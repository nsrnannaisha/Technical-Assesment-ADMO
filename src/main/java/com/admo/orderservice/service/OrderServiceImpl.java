package com.admo.orderservice.service;

import com.admo.orderservice.entity.Order;
import com.admo.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    public OrderServiceImpl(OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order create(Order order) {
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
    public Optional<Order> update(UUID id, Order order) {
        Optional<Order> existing = repository.findById(id);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(repository.save(order));
    }

    @Override
    public boolean delete(UUID id) {

        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);

        return true;
    }
}