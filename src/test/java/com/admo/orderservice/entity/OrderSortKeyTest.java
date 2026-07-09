package com.admo.orderservice.entity;

import com.admo.orderservice.exception.OrderBusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class OrderSortKeyTest {

    @Test
    void resolvesKnownKey() {
        assertThat(OrderSortKey.fromKey("newest")).isEqualTo(OrderSortKey.NEWEST);
    }

    @Test
    void unknownKeyThrows() {
        assertThatThrownBy(() -> OrderSortKey.fromKey("does_not_exist")).isInstanceOf(OrderBusinessException.class);
    }
}