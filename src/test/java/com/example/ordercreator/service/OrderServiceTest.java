package com.example.ordercreator.service;

import com.example.ordercreator.domain.Order;
import com.example.ordercreator.domain.OrderItem;
import com.example.ordercreator.dto.CreateOrderItemRequest;
import com.example.ordercreator.dto.CreateOrderRequest;
import com.example.ordercreator.event.OrderEventPublisher;
import com.example.ordercreator.repository.OrderItemRepository;
import com.example.ordercreator.repository.OrderRepository;
import com.example.ordercreator.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderEventPublisher eventPublisher;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<List<OrderItem>> orderItemsCaptor;

    private UserPrincipal userPrincipal;
    private CreateOrderRequest createOrderRequest;
    private String expectedUserId = "user-123";

    @BeforeEach
    public void setup() {
        userPrincipal = new UserPrincipal(expectedUserId, "user@example.com", 
                Collections.singletonList("ROLE_USER"));

        createOrderRequest = new CreateOrderRequest();
        CreateOrderItemRequest item1 = new CreateOrderItemRequest();
        item1.setProductName("Product A");
        item1.setQuantity(2);
        item1.setUnitPrice(BigDecimal.valueOf(10.99));

        CreateOrderItemRequest item2 = new CreateOrderItemRequest();
        item2.setProductName("Product B");
        item2.setQuantity(1);
        item2.setUnitPrice(BigDecimal.valueOf(29.99));

        createOrderRequest.setItems(List.of(item1, item2));

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void createOrder_ShouldCreateOrderWithAuthenticatedUserId() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        
        Order savedOrder = new Order();
        savedOrder.setOrderId("order-123");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        // When
        Order result = orderService.createOrder(createOrderRequest);
        
        // Then
        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        
        assertEquals(expectedUserId, capturedOrder.getUserId());
        assertNotNull(capturedOrder.getOrderId());
        assertEquals(BigDecimal.valueOf(51.97), capturedOrder.getTotalPrice());
        assertNotNull(capturedOrder.getCreatedAt());
        
        verify(orderItemRepository).saveAll(orderItemsCaptor.capture());
        List<OrderItem> capturedItems = orderItemsCaptor.getValue();
        
        assertEquals(2, capturedItems.size());
        assertEquals("Product A", capturedItems.get(0).getProductName());
        assertEquals(2, capturedItems.get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(10.99), capturedItems.get(0).getUnitPrice());
        assertEquals("Product B", capturedItems.get(1).getProductName());
        
        verify(eventPublisher).publishOrderCreatedEvent(savedOrder);
    }
    
    @Test
    public void createOrder_ShouldThrowException_WhenNoAuthenticatedUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        
        // When & Then
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.createOrder(createOrderRequest);
        });
        
        assertEquals("No authenticated user found", exception.getMessage());
        verify(orderRepository, never()).save(any());
        verify(orderItemRepository, never()).saveAll(any());
        verify(eventPublisher, never()).publishOrderCreatedEvent(any());
    }
}
