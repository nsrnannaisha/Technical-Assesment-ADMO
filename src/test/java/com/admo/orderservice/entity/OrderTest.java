package com.admo.orderservice.entity;

import org.junit.jupiter.api.Test;
import com.admo.orderservice.exception.OrderBusinessException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private Order newOrder() {
        return new Order("Andi Wijaya", List.of(new LineItem("Apple", 3, BigDecimal.valueOf(0.50))));
    }

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

    @Test
    void lineItemSubtotalIsNotRounded() {
        LineItem apple = new LineItem("Apple", 3, new BigDecimal("0.555"));
        assertEquals(new BigDecimal("1.665"), apple.getSubtotal());
    }

    @Test
    void createdOrderCanBePaid() {
        Order order = newOrder();
        order.changeStatus(OrderStatus.PAID, null);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void cannotShipBeforePaid() {
        Order order = newOrder();
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.SHIPPED, null)).isInstanceOf(OrderBusinessException.class).hasMessageContaining("CREATED");
    }

    @Test
    void deliveredOrderCannotBeReactivated() {
        Order order = newOrder();
        order.changeStatus(OrderStatus.PAID, null);
        order.changeStatus(OrderStatus.SHIPPED, null);
        order.changeStatus(OrderStatus.DELIVERED, null);

        assertThatThrownBy(() -> order.changeStatus(OrderStatus.SHIPPED, null)).isInstanceOf(OrderBusinessException.class);
    }

    @Test
    void cancellingWithoutReasonFails() {
        Order order = newOrder();
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.CANCELLED, null))
                .isInstanceOf(OrderBusinessException.class)
                .hasMessageContaining("Reason");
    }

    @Test
    void cancellingWithReasonStoresIt() {
        Order order = newOrder();
        order.changeStatus(OrderStatus.CANCELLED, "Changed my mind");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo("Changed my mind");
    }

    @Test
    void cannotCancelAfterShipped() {
        Order order = newOrder();
        order.changeStatus(OrderStatus.PAID, null);
        order.changeStatus(OrderStatus.SHIPPED, null);

        assertThatThrownBy(() -> order.changeStatus(OrderStatus.CANCELLED, "too late")).isInstanceOf(OrderBusinessException.class);
    }
}