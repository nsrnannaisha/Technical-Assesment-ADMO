package com.admo.orderservice.mapper;

import com.admo.orderservice.dto.CreateOrderRequest;
import com.admo.orderservice.dto.LineItemRequest;
import com.admo.orderservice.dto.OrderResponse;
import com.admo.orderservice.dto.CustomerRequest;
import com.admo.orderservice.dto.CustomerResponse;
import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.entity.Customer;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static Customer toCustomer(String customerName, CustomerRequest req) {
        if (req == null) return null;
        Customer customer = new Customer();
        customer.setCustomerName(customerName);
        customer.setEmail(req.getEmail());
        customer.setPhoneNum(req.getPhoneNum());
        return customer;
    }

    public static Order toEntity(CreateOrderRequest request) {
        Customer customer = toCustomer(request.getCustomerName(), request.getCustomer());
        return new Order(customer, toLineItems(request.getItems()));
    }

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(order.getOrderId(), order.getCustomerName(), toCustomerResponse(order),
                order.getStatus(), order.getTotalAmount(), order.getCreatedAt(), order.getUpdatedAt(), order.getCancellationReason()
        );
    }

    private static CustomerResponse toCustomerResponse(Order order) {
        Customer customer = order.getCustomer();
        if (customer == null) {
            return null;
        }
        return new CustomerResponse(customer.getEmail(), customer.getPhoneNum());
    }

    public static List<LineItem> toLineItems(List<LineItemRequest> requests) {
        return requests.stream().map(item -> new LineItem(item.getProductName(), item.getQuantity(), item.getUnitPrice())).toList();
    }
}