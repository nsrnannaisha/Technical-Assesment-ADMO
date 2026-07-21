package com.admo.orderservice.service;

import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.entity.OrderStatus;
import com.admo.orderservice.entity.Customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface OrderService {
    Order create(Order order);
    Optional<Order> getById(UUID id);
    List<Order> getAll();
    Optional<Order> update(UUID id, Customer customer, List<LineItem> items);
    Optional<Order> update(UUID id, String customerName, List<LineItem> items);
    boolean delete(UUID id);
    Order changeStatus(UUID id, OrderStatus newStatus, String reason);
    Page<Order> getAll(Pageable pageable, String sortKey);
}