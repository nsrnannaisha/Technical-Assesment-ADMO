package com.admo.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    @NotBlank
    @Size(min = 1, max = 255)
    private String customerName;

    @Valid
    @NotEmpty
    @Size(max = 100)
    private List<LineItemRequest> items;
}