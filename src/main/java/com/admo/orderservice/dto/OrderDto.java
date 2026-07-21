package com.admo.orderservice.dto;

import com.admo.orderservice.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderDto {
    private UUID orderId;
    private String customerName;
    private Customer customer;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String cancellationReason;

    @Getter
    @AllArgsConstructor
    public static class Customer {
        private String email;
        private String phoneNum;
    }
}
