package com.admo.orderservice.exception;

import com.admo.orderservice.controller.OrderController;
import com.admo.orderservice.service.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Nested
    class ValidationErrors {

        @Test
        void shouldReturnConsistentBodyForBlankCustomerNameAndEmptyItems() throws Exception {
            String request = """
                    { "customerName":"", "items":[] }
                    """;

            mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details.customerName").exists())
                    .andExpect(jsonPath("$.details.items").exists());
        }
    }

    @Nested
    class TypeMismatchErrors {

        @Test
        void shouldReturnConsistentBodyForInvalidUuid() throws Exception {
            mockMvc.perform(get("/orders/not-a-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    class NotFoundErrors {

        @Test
        void shouldReturnConsistentBodyWhenOrderNotFoundOnGet() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.getById(id)).thenReturn(Optional.empty());

            mockMvc.perform(get("/orders/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Order with id " + id + " was not found"));
        }

        @Test
        void shouldReturnConsistentBodyWhenOrderNotFoundOnUpdate() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.update(org.mockito.ArgumentMatchers.eq(id),
                    org.mockito.ArgumentMatchers.anyString(),
                    org.mockito.ArgumentMatchers.anyList()))
                    .thenReturn(Optional.empty());

            String request = """
                    { "customerName":"Updated", "items":[
                        {"productName":"Apple","quantity":2,"unitPrice":10000}
                    ]}
                    """;

            mockMvc.perform(put("/orders/{id}", id).contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
        }

        @Test
        void shouldReturnConsistentBodyWhenOrderNotFoundOnDelete() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.delete(id)).thenReturn(false);

            mockMvc.perform(delete("/orders/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
        }
    }
}