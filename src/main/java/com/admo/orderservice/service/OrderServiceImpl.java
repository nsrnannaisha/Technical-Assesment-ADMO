package com.admo.orderservice.service;

import com.admo.orderservice.entity.Order;
import com.admo.orderservice.repository.OrderRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

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
    public Order update(UUID id, Order order) {
        return repository.save(order);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }
}