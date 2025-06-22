package com.example.ordercreator.service;

import com.example.ordercreator.domain.Order;
import com.example.ordercreator.domain.OrderItem;
import com.example.ordercreator.dto.CreateOrderRequest;
import com.example.ordercreator.dto.CreateOrderItemRequest;
import com.example.ordercreator.repository.OrderRepository;
import com.example.ordercreator.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // Get the current authenticated user
        String userId = getCurrentUserId();
        log.info("Creating order for user: {}", userId);
        
        Order order = new Order(userId);
        
        List<OrderItem> items = request.getItems().stream()
            .map(itemRequest -> {
                OrderItem item = new OrderItem();
                item.setProductName(itemRequest.getProductName());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnitPrice(itemRequest.getUnitPrice());
                return item;
            })
            .collect(Collectors.toList());

        items.forEach(order::addItem);
        order.calculateTotalPrice();
        
        return orderRepository.save(order);
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof UserPrincipal) {
                return ((UserPrincipal) authentication.getPrincipal()).getUserId();
            }
            return authentication.getName();
        }
        throw new IllegalStateException("No authenticated user found");
    }
}
