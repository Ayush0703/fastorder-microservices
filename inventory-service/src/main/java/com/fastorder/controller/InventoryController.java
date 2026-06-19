package com.fastorder.controller;

import com.fastorder.dto.InventoryReservationRequest;
import com.fastorder.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<Void> reserveInventory(@RequestBody InventoryReservationRequest request) {
        inventoryService.reserveStock(request.getProductId(), request.getQuantity());
        return ResponseEntity.noContent().build();
    }
}
