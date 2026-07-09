package com.admo.orderservice.state;

import com.admo.orderservice.entity.OrderStatus;

public final class OrderStateFactory {

    private OrderStateFactory() {
    }

    public static OrderState from(OrderStatus status) {

        return switch (status) {
            case CREATED -> new CreatedState();
            case PAID -> new PaidState();
            case SHIPPED -> new ShippedState();
            case DELIVERED -> new DeliveredState();
            case CANCELLED -> new CancelledState();
        };
    }
}