package com.admo.orderservice.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class LineItemRequest {

    @NotBlank
    @Size(min = 1, max = 255)
    private String productName;

    @Positive
    @Max(10_000)
    private int quantity;

    @Positive
    @Digits(integer = 17, fraction = 2, message = "unitPrice must have at most 2 decimal places")
    private BigDecimal unitPrice;
}