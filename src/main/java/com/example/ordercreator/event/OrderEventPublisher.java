package com.example.ordercreator.event;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.example.ordercreator.domain.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!dev") // Only use this implementation when not in dev profile
public class OrderEventPublisher {

    private final AmazonSNS snsClient;
    private final ObjectMapper objectMapper;
    
    @Value("${cloud.aws.sns.topic.order-created}")
    private String orderCreatedTopicArn;

    public void publishOrderCreatedEvent(Order order) {
        try {
            var event = new OrderCreatedEvent(order);
            var message = objectMapper.writeValueAsString(event);
            
            log.info("Publishing OrderCreated event for order: {}", order.getId());
            
            snsClient.publish(new PublishRequest()
                .withTopicArn(orderCreatedTopicArn)
                .withMessage(message)
                .withMessageAttributes(
                    Map.of(
                        "eventType", 
                        new MessageAttributeValue()
                            .withDataType("String")
                            .withStringValue("OrderCreated")
                    )
                ));
                
            log.info("Successfully published OrderCreated event");
        } catch (Exception e) {
            log.error("Failed to publish order created event", e);
            throw new RuntimeException("Failed to publish order created event", e);
        }
    }

    private record OrderCreatedEvent(
        UUID orderId,
        String userId,
        BigDecimal totalPrice,
        LocalDateTime createdAt
    ) {
        public OrderCreatedEvent(Order order) {
            this(
                order.getId(),
                order.getUserId(),
                order.getTotalPrice(),
                order.getCreatedAt()
            );
        }
    }
}
