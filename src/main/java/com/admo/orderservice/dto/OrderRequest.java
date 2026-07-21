package com.admo.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderRequest {

    @NotBlank
    @Size(min = 1, max = 255)
    private String customerName;

    @Valid
    private CustomerDto customer;

    @Valid
    @NotEmpty
    @Size(max = 100)
    private List<LineItem> items;

    @Getter
    @NoArgsConstructor
    public static class LineItem {
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
}
