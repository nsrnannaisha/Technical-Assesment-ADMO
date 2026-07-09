package com.admo.orderservice.dto;

import com.admo.orderservice.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StatusTransitionRequest {

    @NotNull
    private OrderStatus status;

    private String reason;
}