package com.admo.orderservice.service;

import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.entity.OrderStatus;
import com.admo.orderservice.exception.OrderBusinessException;
import com.admo.orderservice.exception.OrderNotFoundException;
import com.admo.orderservice.repository.OrderRepository;
import com.admo.orderservice.repository.CustomerRepository;
import com.admo.orderservice.entity.Customer;

import com.admo.orderservice.state.CancelledState;
import com.admo.orderservice.state.PaidState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class OrderServiceTest {
    private OrderRepository repository;
    private CustomerRepository customerRepository;
    private OrderService service;
    private Order order;

    private Customer createCustomer(String name) {
        Customer c = new Customer();
        c.setCustomerName(name);
        c.setEmail("a@b.c");
        c.setPhoneNum("123");
        return c;
    }

    @BeforeEach
    void setUp() {
        repository = mock(OrderRepository.class);
        customerRepository = mock(CustomerRepository.class);
        service = new OrderServiceImpl(repository, customerRepository);
        order = new Order(createCustomer("Ais"), List.of(new LineItem("Apple", 2, new BigDecimal("10.000"))));
    }

    @Test
    void shouldCreateOrder() {
        when(customerRepository.findById("Ais")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(order)).thenReturn(order);
        Order result = service.create(order);
        assertNotNull(result);
        verify(customerRepository).save(any(Customer.class));
        verify(repository).save(order);
    }

    @Test
    void shouldReturnOrderById() {
        UUID id = order.getOrderId();
        when(repository.findById(id)).thenReturn(Optional.of(order));
        Optional<Order> result = service.getById(id);
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
    }

    @Test
    void shouldReturnAllOrders() {
        when(repository.findAll()).thenReturn(List.of(order));
        assertEquals(1, service.getAll().size());
    }

    @Test
    void shouldDeleteOrder() {
        UUID id = order.getOrderId();
        when(repository.existsById(id)).thenReturn(true);
        service.delete(id);
        verify(repository).deleteById(id);
        assertTrue(service.delete(id));
    }

    @Test
    void shouldUpdateExistingOrderPreservingIdStatusAndCreatedAt() {
        UUID id = order.getOrderId();
        var originalCreatedAt = order.getCreatedAt();
        var originalStatus = order.getStatus();

        when(repository.findById(id)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        List<LineItem> newItems = List.of(new LineItem("Mango", 3, new BigDecimal("5.000")));

        when(customerRepository.findById("Updated Name")).thenReturn(Optional.of(createCustomer("Updated Name")));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        Optional<Order> result = service.update(id, "Updated Name", newItems);

        assertTrue(result.isPresent());
        Order updated = result.get();

        assertEquals(id, updated.getOrderId());
        assertEquals(originalCreatedAt, updated.getCreatedAt());
        assertEquals(originalStatus, updated.getStatus());
        assertEquals("Updated Name", updated.getCustomerName());
        assertEquals(new BigDecimal("15.000"), updated.getTotalAmount());

        verify(repository).save(order);
    }

    @Test
    void shouldReuseExistingCustomerWhenCreatingOrder() {
        Customer existing = createCustomer("Ais");
        when(customerRepository.findById("Ais")).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(order)).thenReturn(order);

        Order result = service.create(order);

        assertSame(existing, result.getCustomer());
        verify(customerRepository).save(existing);
        verify(repository).save(order);
    }

    @Test
    void shouldReturnEmptyWhenUpdatingUnknownOrder() {
        UUID unknownId = UUID.randomUUID();
        when(repository.findById(unknownId)).thenReturn(Optional.empty());

        Optional<Order> result = service.update(unknownId, "Name", List.of());

        assertTrue(result.isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnPagedOrdersSortedByHighestTotal() {
        Order low = new Order(createCustomer("A"), List.of(new LineItem("Apple", 1, new BigDecimal("1000"))));
        Order high = new Order(createCustomer("B"), List.of(new LineItem("Apple", 1, new BigDecimal("5000"))));

        when(repository.findAll()).thenReturn(List.of(low, high));

        Page<Order> page = service.getAll(PageRequest.of(0, 10), "highest_total");

        assertEquals(2, page.getTotalElements());
        assertEquals(high, page.getContent().get(0));
    }

    @Test
    void shouldReturnRequestedPage() {
        Order a = new Order(createCustomer("A"), List.of(new LineItem("A",1,BigDecimal.ONE)));
        Order b = new Order(createCustomer("B"), List.of(new LineItem("B",1,BigDecimal.TEN)));
        Order c = new Order(createCustomer("C"), List.of(new LineItem("C",1,new BigDecimal("100"))));

        when(repository.findAll()).thenReturn(List.of(a,b,c));
        Page<Order> page = service.getAll(PageRequest.of(1,1),"highest_total");

        assertEquals(1,page.getContent().size());
    }

    @Test
    void invalidSortKeyThrowsBusinessException() {
        when(repository.findAll()).thenReturn(List.of());
        assertThrows(OrderBusinessException.class, () -> service.getAll(PageRequest.of(0,10),"invalid"));
    }

    @Test
    void shouldReturnFalseWhenDeletingUnknownOrder() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);
        assertFalse(service.delete(id));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void shouldChangeStatus() {
        UUID id = order.getOrderId();

        when(repository.findById(id)).thenReturn(Optional.of(order));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        Order updated = service.changeStatus(id, OrderStatus.PAID, null);

        assertEquals(OrderStatus.PAID, updated.getStatus());
        verify(repository).save(order);
    }

    @Test
    void changeStatusUnknownOrderThrows() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> service.changeStatus(id, OrderStatus.PAID, null));
    }

    @Test
    void paidStateAllowsCancel() {
        PaidState state = new PaidState();
        assertTrue(state.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void paidStateRejectsDelivered() {
        PaidState state = new PaidState();
        assertFalse(state.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void cancelledStateRejectsBlankReason() {
        CancelledState state = new CancelledState();
        assertThrows(OrderBusinessException.class, () -> state.validateTransitionData(""));
    }

    @Test
    void cancelledStateAcceptsReason() {
        CancelledState state = new CancelledState();
        assertDoesNotThrow(() -> state.validateTransitionData("Customer request"));
    }

    @Test
    void equalLineItemsAreEqual() {
        LineItem a = new LineItem("Apple",2,new BigDecimal("10.00"));
        LineItem b = new LineItem("Apple",2,new BigDecimal("10.000"));

        assertEquals(a,b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentLineItemsAreNotEqual() {
        LineItem a = new LineItem("Apple",2,new BigDecimal("10"));
        LineItem b = new LineItem("Apple",3,new BigDecimal("10"));

        assertNotEquals(a,b);
    }
}
