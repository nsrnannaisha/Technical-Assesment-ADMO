package com.admo.orderservice.controller;

import com.admo.orderservice.dto.CustomerCreateRequest;
import com.admo.orderservice.dto.CustomerDetailResponse;
import com.admo.orderservice.dto.CustomerUpdateRequest;
import com.admo.orderservice.entity.Customer;
import com.admo.orderservice.exception.OrderBusinessException;
import com.admo.orderservice.mapper.CustomerMapper;
import com.admo.orderservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomerDetailResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        Customer created = service.create(CustomerMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<CustomerDetailResponse>> getAll() {
        return ResponseEntity.ok(service.getAll().stream().map(CustomerMapper::toResponse).toList());
    }

    @GetMapping("/{customerName}")
    public ResponseEntity<CustomerDetailResponse> getById(@PathVariable String customerName) {
        Customer customer = service.getById(customerName)
                .orElseThrow(() -> new OrderBusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));
        return ResponseEntity.ok(CustomerMapper.toResponse(customer));
    }

    @PutMapping("/{customerName}")
    public ResponseEntity<CustomerDetailResponse> update(@PathVariable String customerName, @Valid @RequestBody CustomerUpdateRequest request) {
        Customer updated = service.update(customerName, CustomerMapper.toEntity(request));
        return ResponseEntity.ok(CustomerMapper.toResponse(updated));
    }

    @DeleteMapping("/{customerName}")
    public ResponseEntity<Void> delete(@PathVariable String customerName) {
        service.delete(customerName);
        return ResponseEntity.noContent().build();
    }
}
