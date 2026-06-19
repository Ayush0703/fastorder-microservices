package com.fastorder.service;

import com.fastorder.client.InventoryClient;
import com.fastorder.dto.InventoryReservationRequest;
import com.fastorder.dto.OrderEvent;
import com.fastorder.entity.Order;
import com.fastorder.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
    }

    public void placeOrder(OrderEvent orderEvent) {
        inventoryClient.reserveInventory(new InventoryReservationRequest(
                orderEvent.getProductId(),
                orderEvent.getQuantity()
        ));

        Order order = new Order();
        order.setProductId(orderEvent.getProductId());
        order.setQuantity(orderEvent.getQuantity());
        order.setTotalPrice(orderEvent.getTotalPrice());

        orderRepository.save(order);
    }
}
