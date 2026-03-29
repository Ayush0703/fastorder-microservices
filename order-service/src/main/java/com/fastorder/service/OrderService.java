package com.fastorder.service;

import com.fastorder.dto.OrderEvent;
import com.fastorder.entity.Order;
import com.fastorder.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    // --- MANUALLY ADD THIS CONSTRUCTOR ---
    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void placeOrder(OrderEvent orderEvent) {
        Order order = new Order();
        order.setProductId(orderEvent.getProductId());
        order.setQuantity(orderEvent.getQuantity());
        order.setTotalPrice(orderEvent.getTotalPrice());

        orderRepository.save(order);

        kafkaTemplate.send("order-topics", orderEvent);
        System.out.println("✅ Database Updated & Kafka Message Sent!");
    }
}
