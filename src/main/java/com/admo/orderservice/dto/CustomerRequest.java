package com.admo.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomerRequest {
    @NotBlank
    @Size(min = 1, max = 50)
    private String phoneNum;

    @NotBlank
    @Size(min = 1, max = 255)
    private String email;
}
