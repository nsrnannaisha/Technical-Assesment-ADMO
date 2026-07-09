package com.admo.orderservice.repository;

import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    OrderRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    void shouldSaveOrder() {
        Order order = new Order("Ais", List.of(new LineItem("Apple", 2, new BigDecimal("10000"))));
        Order saved = repository.save(order);

        assertNotNull(saved.getOrderId());
        assertEquals("Ais", saved.getCustomerName());
        assertEquals(new BigDecimal("20000"), saved.getTotalAmount());
    }

    @Test
    void shouldFindOrderById() {
        Order order = repository.save(new Order("Ais",
                List.of(new LineItem("Apple", 2, new BigDecimal("10000"))))
        );

        assertTrue(repository.findById(order.getOrderId()).isPresent());
    }

    @Test
    void shouldDeleteOrder() {
        Order order = repository.save(new Order("Ais",
                List.of(new LineItem("Apple", 2, new BigDecimal("10000"))))
        );

        repository.deleteById(order.getOrderId());
        assertFalse(repository.findById(order.getOrderId()).isPresent());
    }

    @Test
    void shouldUpdateOrderItemsWithoutError() {
        Order order = repository.save(new Order("Ais",
                List.of(new LineItem("Apple", 2, new BigDecimal("10000")))));

        order.applyUpdate("Budi", List.of(new LineItem("Bread", 1, new BigDecimal("2000"))));
        Order updated = repository.saveAndFlush(order);

        assertEquals("Budi", updated.getCustomerName());
        assertEquals(1, updated.getItems().size());
        assertEquals(new BigDecimal("2000"), updated.getTotalAmount());
    }

    @Test
    void items_preserveInsertionOrder_afterPersistAndReload() {
        Order order = new Order("Andi", List.of(new LineItem("Apple", 3, new BigDecimal("5000")), new LineItem("Bread Loaf", 1, new BigDecimal("2.20"))));
        repository.saveAndFlush(order);
        em.clear();

        Order reloaded = repository.findById(order.getOrderId()).orElseThrow();

        assertThat(reloaded.getItems()).extracting("productName").containsExactly("Apple", "Bread Loaf");
    }
}