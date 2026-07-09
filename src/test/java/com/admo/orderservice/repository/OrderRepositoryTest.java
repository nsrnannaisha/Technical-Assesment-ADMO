package com.admo.orderservice.repository;

import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    @Test
    void shouldSaveOrder() {
        Order order = new Order("Ais", List.of(new LineItem("Apple", 2, new BigDecimal("10.00"))));
        Order saved = repository.save(order);

        assertNotNull(saved.getOrderId());
        assertEquals("Ais", saved.getCustomerName());
        assertEquals(new BigDecimal("20.00"), saved.getTotalAmount());
    }

    @Test
    void shouldFindOrderById() {
        Order order = repository.save(new Order("Ais",
                List.of(new LineItem("Apple", 2, new BigDecimal("10.00"))))
        );

        assertTrue(repository.findById(order.getOrderId()).isPresent());
    }

    @Test
    void shouldDeleteOrder() {
        Order order = repository.save(new Order("Ais",
                List.of(new LineItem("Apple", 2, new BigDecimal("10.00"))))
        );

        repository.deleteById(order.getOrderId());
        assertFalse(repository.findById(order.getOrderId()).isPresent());
    }
}