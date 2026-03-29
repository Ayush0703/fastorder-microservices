package com.fastorder.service;

import com.fastorder.dto.OrderEvent;
import com.fastorder.entity.Inventory;
import com.fastorder.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    // Counter to track retry attempts for testing
    private final AtomicInteger attemptCounter = new AtomicInteger(0);

    @Autowired
    @Lazy
    private InventoryService self;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @KafkaListener(topics = "order-topics", groupId = "inventory-group")
    public void consumeOrder(OrderEvent event) {
        System.out.println("\n📦 Kafka Message Received for: " + event.getProductId());

        // 1. Fill the Cache first (so you can see it in Redis-cli if you comment out line 2)
        Integer stock = self.getStock(event.getProductId());
        System.out.println("🔍 Current Stock (Cache Check): " + stock);

        // 2. Reduce Stock with Retry logic
        self.reduceStock(event.getProductId(), event.getQuantity());
    }

    @Retryable(
            retryFor = { RuntimeException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000, multiplier = 2) // 3s, then 6s
    )
    @CacheEvict(value = "inventory", key = "#productId")
    public void reduceStock(String productId, int quantityToReduce) {
        int currentAttempt = attemptCounter.incrementAndGet();
        System.out.println("🔄 Attempt #" + currentAttempt + " for: " + productId + " at " + java.time.LocalTime.now());

        // --- SIMULATION START ---
        // We force it to fail on Attempt 1 and 2
        if (currentAttempt < 3) {
            System.out.println("⚠️ Simulating Connection Timeout (Fake Error)...");
            throw new RuntimeException("Aerospike/DB node is not responding!");
        }
        // --- SIMULATION END ---

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        int newQuantity = inventory.getQuantity() - quantityToReduce;
        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);

        System.out.println("✅ Success! Updated on Attempt #" + currentAttempt);
        attemptCounter.set(0); // Reset for next message
    }

    @Recover
    public void recover(RuntimeException e, String productId, int quantity) {
        System.err.println("❌ CRITICAL: All retries failed for " + productId + ". Error: " + e.getMessage());
        attemptCounter.set(0);
    }

    @Cacheable(value = "inventory", key = "#productId")
    public Integer getStock(String productId) {
        System.out.println("⚠️ Cache Miss! Fetching from DB for: " + productId);
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::getQuantity)
                .orElse(0);
    }
}
