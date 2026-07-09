package com.admo.orderservice.controller;

import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.entity.OrderStatus;
import com.admo.orderservice.exception.OrderBusinessException;
import com.admo.orderservice.service.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private Order dummyOrder() {
        return new Order("Ais", List.of(new LineItem("Apple", 2, new BigDecimal("10000"))));
    }

    private String validOrderRequest() {
        return """
        {
          "customerName":"Ais",
          "items":[
            {
              "productName":"Apple",
              "quantity":2,
              "unitPrice":10000
            }
          ]
        }
        """;
    }

    @Nested
    class CreateOrder {

        @Test
        void shouldCreateOrder() throws Exception {
            when(orderService.create(any(Order.class))).thenReturn(dummyOrder());

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                    .content(validOrderRequest())).andExpect(status().isCreated());
        }

        @Test
        void shouldRejectCustomerNameTooLong() throws Exception {
            String longName = "A".repeat(256);
            String request = """
            { "customerName":"%s", "items":[
                {"productName":"Apple","quantity":2,"unitPrice":10000}
            ]}
            """.formatted(longName);

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectProductNameTooLong() throws Exception {
            String longName = "A".repeat(256);
            String request = """
            { "customerName":"Ais", "items":[
                {"productName":"%s","quantity":2,"unitPrice":10000}
            ]}
            """.formatted(longName);

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectQuantityExceedingLimit() throws Exception {
            String request = """
            { "customerName":"Ais", "items":[
                {"productName":"Apple","quantity":10001,"unitPrice":10000}
            ]}
            """;

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectTooManyItems() throws Exception {
            StringBuilder items = new StringBuilder();
            for (int i = 0; i < 101; i++) {
                if (i > 0) items.append(",");
                items.append("""
                {"productName":"Item%d","quantity":1,"unitPrice":1000}
                """.formatted(i));
            }
            String request = """
            { "customerName":"Ais", "items":[%s] }
            """.formatted(items);

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectUnitPriceWithTooManyDecimals() throws Exception {
            String request = """
            { "customerName":"Ais", "items":[
                {"productName":"Apple","quantity":2,"unitPrice":10000.555}
            ]}
            """;

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectNonIntegerQuantity() throws Exception {
            String request = """
            { "customerName":"Ais", "items":[
                {"productName":"Apple","quantity":2.5,"unitPrice":10000}
            ]}
            """;

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectEmptyCustomerName() throws Exception {
            String request = """
            { "customerName":"", "items":[
                {"productName":"Apple","quantity":2,"unitPrice":10000}
            ]}
            """;

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectEmptyItemList() throws Exception {
            String request = """
            { "customerName":"Ais", "items":[] }
            """;

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.items").exists());
        }

        @Test
        void shouldRejectNegativeQuantity() throws Exception {
            String request = """
            { "customerName":"Ais", "items":[
                {"productName":"Apple","quantity":-1,"unitPrice":10000}
            ]}
            """;

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.['items[0].quantity']").exists());
        }

        @Test
        void shouldRejectZeroQuantity() throws Exception {
            String request = """
            { "customerName":"Ais", "items":[
                {"productName":"Apple","quantity":0,"unitPrice":10000}
            ]}
            """;

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectNegativeUnitPrice() throws Exception {
            String request = """
            { "customerName":"Ais", "items":[
                {"productName":"Apple","quantity":2,"unitPrice":-100}
            ]}
            """;

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetOrder {

        @Test
        void shouldReturnOrder() throws Exception {
            Order order = dummyOrder();
            when(orderService.getById(order.getOrderId())).thenReturn(Optional.of(order));
            mockMvc.perform(get("/orders/{id}", order.getOrderId())).andExpect(status().isOk());
        }

        @Test
        void shouldReturn404() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.getById(id)).thenReturn(Optional.empty());
            mockMvc.perform(get("/orders/{id}", id)).andExpect(status().isNotFound());
        }

        @Test
        void shouldRejectInvalidUuid() throws Exception {
            mockMvc.perform(get("/orders/abc")).andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetOrders {

        @Test
        void shouldReturnOrders() throws Exception {
            Page<Order> page = new PageImpl<>(List.of(dummyOrder()));
            when(orderService.getAll(any(Pageable.class), eq("newest"))).thenReturn(page);
            mockMvc.perform(get("/orders")).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray()).andExpect(jsonPath("$.content.length()").value(1));
        }

        @Test
        void shouldReturnEmptyList() throws Exception {
            Page<Order> page = new PageImpl<>(List.of());
            when(orderService.getAll(any(Pageable.class), eq("newest"))).thenReturn(page);
            mockMvc.perform(get("/orders")).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray()).andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    @Nested
    class UpdateOrder {

        @Test
        void shouldUpdateOrder() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.update(eq(id), any(String.class), any(List.class))).thenReturn(Optional.of(dummyOrder()));

            mockMvc.perform(put("/orders/{id}", id).contentType(MediaType.APPLICATION_JSON)
                    .content(validOrderRequest())).andExpect(status().isOk());
        }

        @Test
        void shouldReturn404WhenUpdatingUnknownOrder() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.update(eq(id), any(String.class), any(List.class))).thenReturn(Optional.empty());

            String request = """
                    {
                      "customerName":"Updated",
                      "items":[
                        {
                          "productName":"Apple",
                          "quantity":2,
                          "unitPrice":10000
                        }
                      ]
                    }
                    """;

            mockMvc.perform(put("/orders/{id}", id).contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isNotFound());
        }

        @Test
        void shouldRejectInvalidUpdateRequest() throws Exception {
            UUID id = UUID.randomUUID();
            String request = "{}";
            mockMvc.perform(put("/orders/{id}", id).contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isBadRequest());
        }
    }

    @Nested
    class DeleteOrder {

        @Test
        void shouldDeleteOrder() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.delete(id)).thenReturn(true);
            mockMvc.perform(delete("/orders/{id}", id)).andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404WhenDeleteUnknownOrder() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.delete(id)).thenReturn(false);
            mockMvc.perform(delete("/orders/{id}", id)).andExpect(status().isNotFound());
        }

        @Test
        void shouldRejectInvalidUuid() throws Exception {
            mockMvc.perform(delete("/orders/invalid-id")).andExpect(status().isBadRequest());
        }
    }

    @Test
    void patchStatusReturnsUpdatedOrder() throws Exception {
        UUID id = UUID.randomUUID();
        Order updated = dummyOrder();
        updated.changeStatus(OrderStatus.PAID, null);

        when(orderService.changeStatus(eq(id), eq(OrderStatus.PAID), any()))
                .thenReturn(updated);

        mockMvc.perform(patch("/orders/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void patchStatusIllegalTransitionReturns409() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.changeStatus(eq(id), eq(OrderStatus.SHIPPED), any())).thenThrow(new OrderBusinessException("ILLEGAL_STATUS_TRANSITION", "Cannot transition order from PAID to CANCELLED"));
        mockMvc.perform(patch("/orders/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"SHIPPED\"}")).andExpect(status().isConflict());
    }

    @Test
    void listOrders_withCustomSortKey_doesNotBreakOnPageableSortBinding() throws Exception {
        Page<Order> page = new PageImpl<>(List.of(dummyOrder()));
        when(orderService.getAll(any(Pageable.class), eq("highest_total"))).thenReturn(page);

        mockMvc.perform(get("/orders?page=0&size=10&sort=highest_total")).andExpect(status().isOk());
    }

    @Test
    void listOrders_invalidSortKey_returns400() throws Exception {
        when(orderService.getAll(any(Pageable.class), eq("unknown_key"))).thenThrow(new OrderBusinessException("INVALID_SORT_KEY", "Unknown sort key: unknown_key"));

        mockMvc.perform(get("/orders?page=0&size=10&sort=unknown_key")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SORT_KEY"));
    }
}