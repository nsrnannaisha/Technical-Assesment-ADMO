package com.admo.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomerCreateRequest {
    @NotBlank
    @Size(min = 1, max = 255)
    private String customerName;

    @NotBlank
    @Size(min = 1, max = 255)
    private String email;

    @NotBlank
    @Size(min = 1, max = 50)
    private String phoneNum;
}
