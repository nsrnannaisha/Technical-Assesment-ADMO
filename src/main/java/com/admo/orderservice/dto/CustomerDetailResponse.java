package com.admo.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerDetailResponse {
    private String customerName;
    private String email;
    private String phoneNum;
}
