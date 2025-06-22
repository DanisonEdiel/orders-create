package com.example.ordercreator.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "order_id")
    private String orderId;

    @Column(nullable = false, name = "user_id")
    private String userId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, name = "total_price")
    private BigDecimal totalPrice;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    public Order(String userId) {
        this.userId = userId;
        this.orderId = "ORD-" + System.currentTimeMillis();
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
        this.totalPrice = BigDecimal.ZERO;
    }

    public Order addItem(OrderItem item) {
        items.add(item.setOrder(this));
        return this;
    }

    public void calculateTotalPrice() {
        this.totalPrice = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
