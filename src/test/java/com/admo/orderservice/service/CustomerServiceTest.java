package com.admo.orderservice.service;

import com.admo.orderservice.entity.Customer;
import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.exception.OrderBusinessException;
import com.admo.orderservice.repository.CustomerRepository;
import com.admo.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {
    private CustomerRepository customerRepository;
    private OrderRepository orderRepository;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        orderRepository = mock(OrderRepository.class);
        service = new CustomerServiceImpl(customerRepository, orderRepository);
    }

    private Customer createCustomer(String name) {
        Customer customer = new Customer();
        customer.setCustomerName(name);
        customer.setEmail(name + "@example.com");
        customer.setPhoneNum("123");
        return customer;
    }

    private Order createOrder(Customer customer) {
        return new Order(customer, List.of(new LineItem("Apple", 1, new BigDecimal("10"))));
    }

    @Test
    void deleteShouldRejectCustomerThatStillHasOrders() {
        Customer customer = createCustomer("Ais");
        Order order = createOrder(customer);

        when(customerRepository.existsById("Ais")).thenReturn(true);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        OrderBusinessException ex = assertThrows(OrderBusinessException.class, () -> service.delete("Ais"));
        assertEquals("CUSTOMER_HAS_ORDERS", ex.getCode());
        verify(customerRepository, never()).deleteById(anyString());
    }

    @Test
    void deleteShouldRemoveCustomerWithoutOrders() {
        when(customerRepository.existsById("Ais")).thenReturn(true);
        when(orderRepository.findAll()).thenReturn(List.of());

        service.delete("Ais");

        verify(customerRepository).deleteById("Ais");
    }

    @Test
    void updateRenameShouldMoveOrdersToNewCustomer() {
        Customer existing = createCustomer("Ais");
        Customer renamed = createCustomer("Ais Baru");
        Order order = createOrder(existing);

        when(customerRepository.findById("Ais")).thenReturn(Optional.of(existing));
        when(customerRepository.existsById("Ais Baru")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        Customer result = service.update("Ais", renamed);

        assertEquals("Ais Baru", result.getCustomerName());
        assertEquals("Ais Baru", order.getCustomerName());
        verify(customerRepository).delete(existing);
    }
}
