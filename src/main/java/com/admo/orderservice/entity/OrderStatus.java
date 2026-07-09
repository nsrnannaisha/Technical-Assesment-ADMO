package com.admo.orderservice.entity;

import com.admo.orderservice.exception.OrderBusinessException;

public enum OrderStatus {
    CREATED {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return target == PAID || target == CANCELLED;
        }
    },
    PAID {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return target == SHIPPED || target == CANCELLED;
        }
    },
    SHIPPED {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return target == DELIVERED;
        }
    },
    DELIVERED {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return false;
        }
    },
    CANCELLED {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return false;
        }

        @Override
        public void validateTransitionData(String reason) {
            if (reason == null || reason.isBlank()) {
                throw new OrderBusinessException("MISSING_TRANSITION_DATA",
                        "Reason is required to cancel an order");
            }
        }
    };

    public abstract boolean canTransitionTo(OrderStatus target);

    public void validateTransitionData(String reason) {
    }
}