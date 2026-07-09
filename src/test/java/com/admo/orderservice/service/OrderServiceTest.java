package com.admo.orderservice.service;

import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private OrderRepository repository;
    private OrderService service;
    private Order order;

    @BeforeEach
    void setUp() {
        repository = mock(OrderRepository.class);
        service = new OrderServiceImpl(repository);
        order = new Order("Ais", List.of(new LineItem("Apple", 2, new BigDecimal("10.000"))));
    }

    @Test
    void shouldCreateOrder() {
        when(repository.save(order)).thenReturn(order);
        Order result = service.create(order);
        assertNotNull(result);
        verify(repository).save(order);
    }

    @Test
    void shouldReturnOrderById() {
        UUID id = order.getOrderId();
        when(repository.findById(id)).thenReturn(Optional.of(order));
        Optional<Order> result = service.getById(id);
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
    }

    @Test
    void shouldReturnAllOrders() {
        when(repository.findAll()).thenReturn(List.of(order));
        assertEquals(1, service.getAll().size());
    }

    @Test
    void shouldDeleteOrder() {
        UUID id = order.getOrderId();
        when(repository.existsById(id)).thenReturn(true);
        service.delete(id);
        verify(repository).deleteById(id);
        assertTrue(service.delete(id));
    }

    @Test
    void shouldUpdateExistingOrderPreservingIdStatusAndCreatedAt() {
        UUID id = order.getOrderId();
        var originalCreatedAt = order.getCreatedAt();
        var originalStatus = order.getStatus();

        when(repository.findById(id)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        List<LineItem> newItems = List.of(new LineItem("Mango", 3, new BigDecimal("5.000")));

        Optional<Order> result = service.update(id, "Updated Name", newItems);

        assertTrue(result.isPresent());
        Order updated = result.get();

        assertEquals(id, updated.getOrderId());
        assertEquals(originalCreatedAt, updated.getCreatedAt());
        assertEquals(originalStatus, updated.getStatus());
        assertEquals("Updated Name", updated.getCustomerName());
        assertEquals(new BigDecimal("15.000"), updated.getTotalAmount());

        verify(repository).save(order);
    }

    @Test
    void shouldReturnEmptyWhenUpdatingUnknownOrder() {
        UUID unknownId = UUID.randomUUID();
        when(repository.findById(unknownId)).thenReturn(Optional.empty());

        Optional<Order> result = service.update(unknownId, "Name", List.of());

        assertTrue(result.isEmpty());
        verify(repository, never()).save(any());
    }
}
