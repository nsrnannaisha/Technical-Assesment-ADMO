package com.admo.orderservice.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderTest {

    @Test
    void calculateTotalAmountForSingleItem() {
        LineItem apple = new LineItem("Apple", 2, new BigDecimal("10.000"));
        Order order = new Order("Ais", List.of(apple));

        assertEquals(new BigDecimal("20.000"), order.getTotalAmount());
    }

    @Test
    void calculateTotalAmountForMultipleItem() {
        LineItem apple = new LineItem("Apple", 2, new BigDecimal("10.000"));
        LineItem mango = new LineItem("mango", 4, new BigDecimal("15.000"));
        Order order = new Order("Ais", List.of(apple, mango));

        assertEquals(new BigDecimal("80.000"), order.getTotalAmount());
    }
}