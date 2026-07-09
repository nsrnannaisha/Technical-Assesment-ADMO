package com.admo.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class LineItemRequest {

    @NotBlank
    private String productName;

    @Positive
    private int quantity;

    @Positive
    private BigDecimal unitPrice;
}