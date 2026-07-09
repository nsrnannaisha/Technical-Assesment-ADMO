package com.admo.orderservice.entity;

import com.admo.orderservice.exception.OrderBusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

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

    @Test
    void highestTotalComparator_sortsDescendingByTotalAmount() {
        Order low = new Order("A", List.of(new LineItem("Item", 1, new BigDecimal("1000"))));
        Order high = new Order("B", List.of(new LineItem("Item", 1, new BigDecimal("100000"))));
        Order mid = new Order("C", List.of(new LineItem("Item", 1, new BigDecimal("50000"))));

        List<Order> sorted = Stream.of(low, high, mid).sorted(OrderSortKey.HIGHEST_TOTAL.comparator()).toList();
        assertThat(sorted).extracting(Order::getTotalAmount).containsExactly(new BigDecimal("100000"), new BigDecimal("50000"), new BigDecimal("1000"));
    }
}