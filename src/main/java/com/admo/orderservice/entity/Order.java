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

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "customer_name", referencedColumnName = "customerName", nullable = false)
    private Customer customer;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @OrderColumn(name = "item_index")
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

    public Order(Customer customer, List<LineItem> items) {
        this.orderId = UUID.randomUUID();
        this.customer = customer;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.totalAmount = calculateTotalAmount();
    }

    public void assignCustomer(Customer customer) {
        this.customer = customer;
    }

    private BigDecimal calculateTotalAmount() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream().map(LineItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void applyUpdate(Customer customer, List<LineItem> items) {
        boolean itemsChanged = !this.items.equals(items);

        if (status != OrderStatus.CREATED && itemsChanged) {
            throw new OrderBusinessException(
                    "ITEMS_IMMUTABLE",
                    "Line items cannot be modified after payment"
            );
        }

        this.customer = customer;

        if (status == OrderStatus.CREATED) {
            this.items.clear();
            this.items.addAll(items);
            this.totalAmount = calculateTotalAmount();
        }

        this.updatedAt = LocalDateTime.now();
    }

    private void validateStatusTransition(OrderStatus newStatus, String reason) {
        OrderStateFactory.from(status).validateTransition(newStatus);
        OrderStateFactory.from(newStatus).validateTransitionData(reason);
    }

    public void changeStatus(OrderStatus newStatus, String reason) {
        validateStatusTransition(newStatus, reason);
        this.status = newStatus;

        if (newStatus == OrderStatus.CANCELLED) {
            this.cancellationReason = reason;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public String getCustomerName() {
        return customer != null ? customer.getCustomerName() : null;
    }
}
