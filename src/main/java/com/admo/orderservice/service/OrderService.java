package com.admo.orderservice.service;

import com.admo.orderservice.entity.Order;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface OrderService {
    Order create(Order order);
    Optional<Order> getById(UUID id);
    List<Order> getAll();
    Optional<Order> update(UUID id, Order order);
    boolean delete(UUID id);
}