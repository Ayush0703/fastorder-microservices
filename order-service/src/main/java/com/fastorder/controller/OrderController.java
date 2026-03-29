package com.fastorder.controller;


import com.fastorder.dto.OrderEvent;
import com.fastorder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // --- ADD THIS MANUAL CONSTRUCTOR ---
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public String placeOrder(@RequestBody OrderEvent orderEvent) {
        orderService.placeOrder(orderEvent);
        return "Order Placed Successfully!";
    }
}