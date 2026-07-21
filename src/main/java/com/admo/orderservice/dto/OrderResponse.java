package com.admo.orderservice.dto;

import com.admo.orderservice.entity.OrderStatus;
import com.admo.orderservice.dto.CustomerResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderResponse {

    private UUID orderId;
    private String customerName;
    private CustomerResponse customer;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String cancellationReason;
}
