package com.fastorder.service;

import com.fastorder.entity.Inventory;
import com.fastorder.repository.InventoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "#productId")
    public void reserveStock(String productId, int quantityToReserve) {
        if (quantityToReserve <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (inventory.getQuantity() < quantityToReserve) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }

        inventory.setQuantity(inventory.getQuantity() - quantityToReserve);
        inventoryRepository.save(inventory);
    }

    @Cacheable(value = "inventory", key = "#productId")
    public Integer getStock(String productId) {
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::getQuantity)
                .orElse(0);
    }
}
