package com.fastorder.service;

import com.fastorder.entity.Inventory;
import com.fastorder.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest // Boots up the whole Spring application context
@Testcontainers // Automatically starts and stops Docker containers
class InventoryServiceIntegrationTest {

    // 1. Setup a real MySQL Docker Container
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_inventory_db")
            .withUsername("test")
            .withPassword("test");

    // 2. Setup a real Redis Docker Container
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0")
            .withExposedPorts(6379);

    @Autowired
    private InventoryService inventoryService; // Real service instance

    @Autowired
    private InventoryRepository inventoryRepository; // Real repo instance

    // 3. Override application properties dynamically to connect to these containers
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void cleanDatabase() {
        inventoryRepository.deleteAll(); // Start with a fresh DB state
    }

    @Test
    void shouldFetchStockFromDbOnCacheMissAndThenUseCacheOnSecondCall() {
        // Arrange: Seed real data into the Docker MySQL database
        Inventory initialItem = new Inventory();
        initialItem.setProductId("PROD-ABC");
        initialItem.setQuantity(50);
        inventoryRepository.save(initialItem);

        // Act - Call 1: This should cause a Cache Miss and hit the real MySQL DB container
        Integer stockFirstCall = inventoryService.getStock("PROD-ABC");
        assertEquals(50, stockFirstCall);

        // Act - Call 2: This should fetch directly from the real Redis container (Cache Hit)
        Integer stockSecondCall = inventoryService.getStock("PROD-ABC");
        assertEquals(50, stockSecondCall);

        // Verify: Modify DB directly to check if cache intercepts the call
        initialItem.setQuantity(10);
        inventoryRepository.save(initialItem);

        // Even though DB has 10, service should still return cached value 50 from Redis
        Integer stockCachedCall = inventoryService.getStock("PROD-ABC");
        assertEquals(50, stockCachedCall);
    }
}