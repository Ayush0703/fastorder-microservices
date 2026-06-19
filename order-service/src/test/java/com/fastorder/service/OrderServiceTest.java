package com.fastorder.service;

import com.fastorder.dto.OrderEvent;
import com.fastorder.entity.Order;
import com.fastorder.repository.OrderRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
public class OrderServiceTest {

    private static HttpServer inventoryServer;
    private static final AtomicReference<String> receivedRequest = new AtomicReference<>();

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private OrderRepository orderRepository;

    @BeforeAll
    static void startInventoryServer() throws IOException {
        inventoryServer = HttpServer.create(new InetSocketAddress(0), 0);
        inventoryServer.createContext("/api/inventory/reservations", OrderServiceTest::handleReservation);
        inventoryServer.start();
    }

    @AfterAll
    static void stopInventoryServer() {
        inventoryServer.stop(0);
    }

    @DynamicPropertySource
    static void inventoryServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("inventory.service.url",
                () -> "http://localhost:" + inventoryServer.getAddress().getPort());
    }

    @Test
    void placeOrderReservesInventoryThroughFeignBeforeSavingOrder() {
        orderService.placeOrder(new OrderEvent("PROD-ABC", 2, 49.98));

        String requestBody = receivedRequest.get();
        assertTrue(requestBody.contains("\"productId\":\"PROD-ABC\""));
        assertTrue(requestBody.contains("\"quantity\":2"));

        verify(orderRepository).save(argThat((Order order) ->
                order.getProductId().equals("PROD-ABC")
                        && order.getQuantity() == 2
                        && order.getTotalPrice() == 49.98
        ));
    }

    private static void handleReservation(HttpExchange exchange) throws IOException {
        receivedRequest.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }
}
