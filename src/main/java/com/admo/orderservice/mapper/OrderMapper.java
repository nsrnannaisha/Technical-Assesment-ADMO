package com.admo.orderservice.mapper;

import com.admo.orderservice.dto.OrderDto;
import com.admo.orderservice.dto.OrderRequest;
import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.entity.Customer;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static Customer toCustomer(String customerName, OrderRequest.Customer req) {
        if (req == null) return null;
        Customer customer = new Customer();
        customer.setCustomerName(customerName);
        customer.setEmail(req.getEmail());
        customer.setPhoneNum(req.getPhoneNum());
        return customer;
    }

    public static Order toEntity(OrderRequest request) {
        Customer customer = toCustomer(request.getCustomerName(), request.getCustomer());
        return new Order(customer, toLineItems(request.getItems()));
    }

    public static OrderDto toResponse(Order order) {
        return new OrderDto(order.getOrderId(), order.getCustomerName(), toCustomerResponse(order),
                order.getStatus(), order.getTotalAmount(), order.getCreatedAt(), order.getUpdatedAt(), order.getCancellationReason()
        );
    }

    private static OrderDto.Customer toCustomerResponse(Order order) {
        Customer customer = order.getCustomer();
        if (customer == null) {
            return null;
        }
        return new OrderDto.Customer(customer.getEmail(), customer.getPhoneNum());
    }

    public static List<LineItem> toLineItems(List<OrderRequest.LineItem> requests) {
        return requests.stream().map(item -> new LineItem(item.getProductName(), item.getQuantity(), item.getUnitPrice())).toList();
    }
}
