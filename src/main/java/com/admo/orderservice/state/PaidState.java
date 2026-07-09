package com.admo.orderservice.state;

import com.admo.orderservice.entity.OrderStatus;

public class PaidState extends OrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PAID;
    }

    @Override
    public boolean canTransitionTo(OrderStatus target) {
        return target == OrderStatus.SHIPPED
                || target == OrderStatus.CANCELLED;
    }
}