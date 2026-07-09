package com.admo.orderservice.state;

import com.admo.orderservice.entity.OrderStatus;

public class ShippedState extends OrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.SHIPPED;
    }

    @Override
    public boolean canTransitionTo(OrderStatus target) {
        return target == OrderStatus.DELIVERED;
    }
}