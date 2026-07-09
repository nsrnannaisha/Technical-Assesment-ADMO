package com.admo.orderservice.state;

import com.admo.orderservice.entity.OrderStatus;
import com.admo.orderservice.exception.OrderBusinessException;

public class CancelledState extends OrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }

    @Override
    public boolean canTransitionTo(OrderStatus target) {
        return false;
    }

    @Override
    public void validateTransitionData(String reason) {

        if (reason == null || reason.isBlank()) {
            throw new OrderBusinessException(
                    "MISSING_TRANSITION_DATA",
                    "Reason is required to cancel an order"
            );
        }
    }
}