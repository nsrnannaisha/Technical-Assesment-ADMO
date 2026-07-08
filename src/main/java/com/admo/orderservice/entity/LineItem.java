package com.admo.orderservice.entity;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LineItem {

    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;

    public LineItem(String productName, int quantity, BigDecimal unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}