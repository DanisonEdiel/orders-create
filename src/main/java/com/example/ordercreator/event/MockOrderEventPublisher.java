package com.example.ordercreator.event;

import com.example.ordercreator.domain.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.sns.AmazonSNS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * Mock implementation of OrderEventPublisher for local development
 * This class will be used when the "dev" profile is active
 */
@Component
@Profile("dev")
@Slf4j
public class MockOrderEventPublisher extends OrderEventPublisher {

    @Value("${cloud.aws.sns.topic.order-created:arn:aws:sns:us-east-1:000000000000:mock-order-created}")
    private String orderCreatedTopicArn;

    public MockOrderEventPublisher(AmazonSNS snsClient, ObjectMapper objectMapper) {
        super(snsClient, objectMapper);
    }

    @Override
    public void publishOrderCreatedEvent(Order order) {
        log.info("MOCK: Publishing OrderCreated event for order: {}", order.getOrderId());
        log.info("MOCK: Event details: userId={}, totalPrice={}, createdAt={}", 
                order.getUserId(), order.getTotalPrice(), order.getCreatedAt());
        log.info("MOCK: Successfully published OrderCreated event");
    }
}
