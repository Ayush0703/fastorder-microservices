package com.fastorder.repository;

import com.fastorder.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // Custom query to find stock by Product ID
    Optional<Inventory> findByProductId(String productId);
}