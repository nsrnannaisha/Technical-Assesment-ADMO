package com.admo.orderservice.state;

import com.admo.orderservice.entity.OrderStatus;
import com.admo.orderservice.exception.OrderBusinessException;

public abstract class OrderState {
    public abstract OrderStatus getStatus();
    public abstract boolean canTransitionTo(OrderStatus target);
    public void validateTransitionData(String reason) {}

    public void validateTransition(OrderStatus target) {
        if (!canTransitionTo(target)) {
            throw new OrderBusinessException("ILLEGAL_STATUS_TRANSITION", "Cannot transition order from " + getStatus() + " to " + target);
        }
    }
}