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
    @Digits(integer = 17, fraction = 0, message = "unitPrice must be a whole number (IDR)")
    private BigDecimal unitPrice;
}