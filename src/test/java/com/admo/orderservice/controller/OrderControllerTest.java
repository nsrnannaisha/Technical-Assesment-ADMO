package com.admo.orderservice.controller;

import com.admo.orderservice.entity.LineItem;
import com.admo.orderservice.entity.Order;
import com.admo.orderservice.service.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Nested
    class CreateOrder {

        @Test
        void shouldCreateOrder() throws Exception {
            when(orderService.create(any(Order.class))).thenReturn(dummyOrder());

            String request = """
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

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                    .content(request)).andExpect(status().isCreated());
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
            when(orderService.getAll()).thenReturn(List.of(dummyOrder()));
            mockMvc.perform(get("/orders")) .andExpect(status().isOk());
        }

        @Test
        void shouldReturnEmptyList() throws Exception {
            when(orderService.getAll()).thenReturn(List.of());
            mockMvc.perform(get("/orders")).andExpect(status().isOk());
        }
    }

    @Nested
    class UpdateOrder {

        @Test
        void shouldUpdateOrder() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.update(eq(id), any(Order.class))).thenReturn(Optional.of(dummyOrder()));

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

            mockMvc.perform(put("/orders/{id}", id).contentType(MediaType.APPLICATION_JSON)
                    .content(request)).andExpect(status().isOk());
        }

        @Test
        void shouldReturn404WhenUpdatingUnknownOrder() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.update(eq(id), any(Order.class))).thenReturn(Optional.empty());

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
}