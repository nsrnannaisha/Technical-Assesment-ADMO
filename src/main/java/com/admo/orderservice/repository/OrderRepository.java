package com.admo.orderservice.repository;

import com.admo.orderservice.entity.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
}