package com.admo.orderservice.mapper;

import com.admo.orderservice.dto.CreateOrderRequest;
import com.admo.orderservice.dto.LineItemRequest;
import com.admo.orderservice.dto.OrderResponse;
import com.admo.orderservice.dto.UpdateOrderRequest;
import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static Order toEntity(CreateOrderRequest request) {

        return new Order(
                request.getCustomerName(),
                toLineItems(request.getItems())
        );
    }

    public static Order toEntity(UpdateOrderRequest request) {

        return new Order(
                request.getCustomerName(),
                toLineItems(request.getItems())
        );
    }

    public static OrderResponse toResponse(Order order) {

        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private static List<LineItem> toLineItems(List<LineItemRequest> requests) {

        return requests.stream()
                .map(item -> new LineItem(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();
    }
}