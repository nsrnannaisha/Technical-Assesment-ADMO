package com.admo.orderservice.state;

import com.admo.orderservice.entity.OrderStatus;

public class DeliveredState extends OrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.DELIVERED;
    }

    @Override
    public boolean canTransitionTo(OrderStatus target) {
        return false;
    }
}