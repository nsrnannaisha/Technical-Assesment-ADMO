package com.admo.orderservice.entity;

import com.admo.orderservice.exception.OrderBusinessException;
import com.admo.orderservice.state.OrderState;
import com.admo.orderservice.state.OrderStateFactory;
import lombok.Getter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String customerName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<LineItem> items;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private String cancellationReason;

    public Order(String customerName, List<LineItem> items) {
        this.orderId = UUID.randomUUID();
        this.customerName = customerName;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.totalAmount = calculateTotalAmount();
    }

    private BigDecimal calculateTotalAmount() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream().map(LineItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void applyUpdate(String customerName, List<LineItem> items) {
        this.customerName = customerName;
        if (this.status == OrderStatus.CREATED) {
            this.items.clear();
            this.items.addAll(items);
            this.totalAmount = calculateTotalAmount();
        } else if (!this.items.equals(items)) {
            throw new OrderBusinessException("ITEMS_IMMUTABLE", "Order " + orderId + " has been paid; line items can no longer be modified");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(OrderStatus newStatus, String reason) {
        OrderState currentState = OrderStateFactory.from(this.status);
        currentState.validateTransition(newStatus, reason);
        OrderState targetState = OrderStateFactory.from(newStatus);
        targetState.validateTransitionData(reason);
        this.status = newStatus;

        if (newStatus == OrderStatus.CANCELLED) {
            this.cancellationReason = reason;
        }

        this.updatedAt = LocalDateTime.now();
    }
}