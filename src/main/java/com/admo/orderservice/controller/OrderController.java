package com.admo.orderservice.controller;

import com.admo.orderservice.dto.CreateOrderRequest;
import com.admo.orderservice.dto.OrderResponse;
import com.admo.orderservice.dto.UpdateOrderRequest;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.mapper.OrderMapper;
import com.admo.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request) {

        Order order = service.create(OrderMapper.toEntity(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(OrderMapper.toResponse(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id) {

        return service.getById(id).map(OrderMapper::toResponse)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        List<OrderResponse> responses = service.getAll().stream().map(OrderMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateOrderRequest request) {
        Optional<Order> updated = service.update(id, request.getCustomerName(), OrderMapper.toLineItems(request.getItems()));
        return updated.map(OrderMapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}