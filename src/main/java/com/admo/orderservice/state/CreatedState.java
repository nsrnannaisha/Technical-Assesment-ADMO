package com.admo.orderservice.state;

import com.admo.orderservice.entity.OrderStatus;

public class CreatedState extends OrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CREATED;
    }

    @Override
    public boolean canTransitionTo(OrderStatus target) {
        return target == OrderStatus.PAID
                || target == OrderStatus.CANCELLED;
    }
}