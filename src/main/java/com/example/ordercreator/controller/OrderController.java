package com.example.ordercreator.controller;

import com.example.ordercreator.dto.CreateOrderRequest;
import com.example.ordercreator.event.OrderEventPublisher;
import com.example.ordercreator.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final OrderEventPublisher orderEventPublisher;

    @Operation(
        summary = "Create a new order", 
        description = "Creates a new order with the authenticated user's ID",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("Received order creation request");
        var order = orderService.createOrder(request);
        log.info("Created order with ID: {}", order.getOrderId());
        orderEventPublisher.publishOrderCreatedEvent(order);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of(
                "orderId", order.getOrderId(),
                "status", "CREATED",
                "message", "Order created successfully"
            ));
    }
}
