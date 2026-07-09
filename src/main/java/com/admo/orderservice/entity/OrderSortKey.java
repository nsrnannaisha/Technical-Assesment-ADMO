package com.admo.orderservice.entity;

import com.admo.orderservice.exception.OrderBusinessException;

import java.util.Arrays;
import java.util.Comparator;

public enum OrderSortKey {

    NEWEST("newest") {
        @Override
        public Comparator<Order> comparator() {
            return Comparator.comparing(Order::getCreatedAt).reversed();
        }
    },
    HIGHEST_TOTAL("highest_total") {
        @Override
        public Comparator<Order> comparator() {
            return Comparator.comparing(Order::getTotalAmount).reversed();
        }
    },
    OLDEST_UNPAID("oldest_unpaid") {
        @Override
        public Comparator<Order> comparator() {
            return Comparator
                    .<Order, Boolean>comparing(o -> o.getStatus() != OrderStatus.CREATED)
                    .thenComparing(Order::getCreatedAt);
        }
    };

    private final String key;

    OrderSortKey(String key) {
        this.key = key;
    }

    public abstract Comparator<Order> comparator();

    public static OrderSortKey fromKey(String key) {
        return Arrays.stream(values()).filter(k -> k.key.equals(key)).findFirst().orElseThrow(() -> new OrderBusinessException("INVALID_SORT_KEY", "Unknown sort key: " + key));
    }
}