package com.admo.orderservice.controller;

import com.admo.orderservice.dto.CreateOrderRequest;
import com.admo.orderservice.dto.OrderResponse;
import com.admo.orderservice.dto.StatusTransitionRequest;
import com.admo.orderservice.dto.UpdateOrderRequest;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.exception.OrderNotFoundException;
import com.admo.orderservice.mapper.OrderMapper;
import com.admo.orderservice.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
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
        Order order = service.getById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAll(@RequestParam(defaultValue = "newest") String sort, Pageable pageable) {
        Page<OrderResponse> result = service.getAll(pageable, sort).map(OrderMapper::toResponse);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateOrderRequest request) {
        Order updated = service.update(id, request.getCustomerName(), OrderMapper.toLineItems(request.getItems())).orElseThrow(() -> new OrderNotFoundException(id));
        return ResponseEntity.ok(OrderMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        if (!service.delete(id)) {
            throw new OrderNotFoundException(id);
        }

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> changeStatus(@PathVariable UUID id, @Valid @RequestBody StatusTransitionRequest request) {
        Order updated = service.changeStatus(id, request.getStatus(), request.getReason());
        return ResponseEntity.ok(OrderMapper.toResponse(updated));
    }
}